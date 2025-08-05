package com.adamo.vrspfab.payments;


import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@RequiredArgsConstructor
@Service("stripePaymentProvider")
public class StripePaymentProvider implements PaymentProvider {
    @Value("${stripe.api.secretKey}")
    private String stripeSecretKey;


    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final DisputeRepository disputeRepository;

    private static final int MAX_RETRIES = 3;

    @Override
    @Transactional
    public PaymentResponseDto processPayment(PaymentRequestDto requestDto) {
        try {
            Payment payment = paymentRepository.findByReservationId(requestDto.getReservationId())
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found for reservation ID: " + requestDto.getReservationId()));
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(requestDto.getAmount() * 100) // Convert to cents
                    .setCurrency(requestDto.getCurrency().toLowerCase())
                    .setPaymentMethod(requestDto.getPaymentMethodId())
                    .setConfirmationMethod(PaymentIntentCreateParams.ConfirmationMethod.AUTOMATIC)
                    .setConfirm(true)
                    .build();
            PaymentIntent paymentIntent = retry(() -> PaymentIntent.create(params), MAX_RETRIES);
            payment.setTransactionId(paymentIntent.getId());
            payment.setStatus(mapStripeStatus(paymentIntent.getStatus()));
            paymentRepository.save(payment);
            return new PaymentResponseDto(payment.getId(), paymentIntent.getId(), paymentIntent.getStatus(), null);
        } catch (StripeException e) {
            throw new PaymentException("Stripe payment failed: " + e.getMessage());
        }
    }

    @Override
    public SessionResponseDto createPaymentSession(SessionRequestDto requestDto) {
        try {
            SessionCreateParams.Builder builder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(requestDto.getSuccessUrl())
                    .setCancelUrl(requestDto.getCancelUrl())
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency(requestDto.getCurrency())
                                    .setUnitAmount(requestDto.getAmount() * 100) // Convert to cents
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName("Reservation Payment")
                                            .build())
                                    .build())
                            .setQuantity(1L)
                            .build());

            // Add metadata entries
            builder.putMetadata("reservation_id", requestDto.getReservationId().toString());

            SessionCreateParams params = builder.build();

            Session session = retry(() -> Session.create(params), MAX_RETRIES);
            return new SessionResponseDto(session.getId(), session.getUrl());
        } catch (StripeException e) {
            throw new PaymentException("Stripe session creation failed: " + e.getMessage());
        }
    }

    @Override
    public PaymentResponseDto getPaymentStatus(Long paymentId) {
        try {
            Payment payment = paymentRepository.findWithDetailsById(paymentId)
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
            PaymentIntent paymentIntent = PaymentIntent.retrieve(payment.getTransactionId());
            payment.setStatus(mapStripeStatus(paymentIntent.getStatus()));
            paymentRepository.save(payment);
            return new PaymentResponseDto(paymentId, paymentIntent.getId(), paymentIntent.getStatus(), null);
        } catch (StripeException e) {
            throw new PaymentException("Stripe status retrieval failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public RefundResponseDto processRefund(RefundRequestDto requestDto) {
        try {
            Payment payment = paymentRepository.findWithDetailsById(requestDto.getPaymentId())
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + requestDto.getPaymentId()));
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(payment.getTransactionId())
                    .setAmount(requestDto.getAmount() * 100)
                    .setReason(RefundCreateParams.Reason.valueOf(requestDto.getReason()))
                    .build();
            com.stripe.model.Refund stripeRefund = retry(() -> com.stripe.model.Refund.create(params), MAX_RETRIES);
            Refund refund = new Refund();
            refund.setPayment(payment);
            refund.setAmount(BigDecimal.valueOf(requestDto.getAmount()));
            refund.setCurrency(payment.getCurrency());
            refund.setReason(requestDto.getReason());
            refund.setStatus(com.adamo.vrspfab.payments.RefundStatus.PROCESSED);
            refund.setProcessedAt(LocalDateTime.now());
            refund = refundRepository.save(refund);
            payment.setStatus(com.adamo.vrspfab.payments.PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
            return new RefundResponseDto(refund.getId(), RefundStatus.valueOf(stripeRefund.getStatus()));
        } catch (StripeException e) {
            throw new PaymentException("Stripe refund failed: " + e.getMessage());
        }
    }


    private com.adamo.vrspfab.payments.PaymentStatus mapStripeStatus(String stripeStatus) {
        return switch (stripeStatus.toLowerCase()) {
            case "succeeded" -> PaymentStatus.COMPLETED;
            case "failed" -> PaymentStatus.FAILED;
            default -> PaymentStatus.PENDING;
        };
    }


    private <T> T retry(StripeOperation<T> operation, int maxRetries) throws StripeException {
        int attempts = 0;
        StripeException lastException = null;

        while (attempts < maxRetries) {
            try {
                return operation.execute();
            } catch (StripeException e) {
                lastException = e;
                attempts++;
                if (attempts >= maxRetries) throw lastException;
                try {
                    Thread.sleep(1000L * attempts); // Exponential backoff
                } catch (InterruptedException ignored) {}
            }
        }
        throw lastException; // Final re-throw
    }


    @FunctionalInterface
    interface StripeOperation<T> {
        T execute() throws StripeException;
    }
}