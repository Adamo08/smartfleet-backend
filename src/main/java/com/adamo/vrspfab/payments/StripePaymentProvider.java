package com.adamo.vrspfab.payments;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service("stripePaymentProvider")
public class StripePaymentProvider implements PaymentProvider {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;

    @Override
    @Transactional
    public PaymentResponseDto processPayment(PaymentRequestDto requestDto) {
        throw new UnsupportedOperationException("Direct payment processing is deprecated in favor of the secure Checkout Session flow.");
    }

    @Override
    @Transactional
    public SessionResponseDto createPaymentSession(SessionRequestDto requestDto) {
        try {
            // Create a new Payment entity in PENDING status before creating the session
            Payment payment = createPendingPayment(requestDto);

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(requestDto.getSuccessUrl())
                    .setCancelUrl(requestDto.getCancelUrl())
                    .putMetadata("payment_id", payment.getId().toString()) // Use our internal DB ID
                    .setClientReferenceId(requestDto.getReservationId().toString())
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency(requestDto.getCurrency().toLowerCase())
                                    .setUnitAmount(requestDto.getAmount().multiply(BigDecimal.valueOf(100)).longValue())
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName("Reservation Payment #" + requestDto.getReservationId())
                                            .build())
                                    .build())
                            .setQuantity(1L)
                            .build())
                    .build();

            Session session = retryStripeCall(() -> Session.create(params));

            // Associate the Stripe session ID with our payment record
            payment.setTransactionId(session.getPaymentIntent()); // The Payment Intent ID is the key
            paymentRepository.save(payment);

            return new SessionResponseDto(session.getId(), session.getUrl());
        } catch (StripeException e) {
            log.error("Stripe session creation failed", e);
            throw new PaymentException("Stripe session creation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentResponseDto getPaymentStatus(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found: " + paymentId));

        if (payment.getTransactionId() == null) {
            throw new PaymentException("Payment has no transaction ID to check status.");
        }

        try {
            PaymentIntent paymentIntent = retryStripeCall(() -> PaymentIntent.retrieve(payment.getTransactionId()));
            return new PaymentResponseDto(paymentId, paymentIntent.getId(), paymentIntent.getStatus(), null);
        } catch (StripeException e) {
            log.error("Stripe status retrieval failed for payment ID {}: {}", paymentId, e.getMessage());
            throw new PaymentException("Stripe status retrieval failed: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public RefundResponseDto processRefund(RefundRequestDto requestDto) {
        Payment payment = paymentRepository.findById(requestDto.getPaymentId())
                .orElseThrow(() -> new PaymentException("Cannot refund non-existent payment with ID: " + requestDto.getPaymentId()));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new PaymentException("Cannot refund a payment that is not completed. Current status: " + payment.getStatus());
        }

        try {
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(payment.getTransactionId())
                    .setAmount(requestDto.getAmount() * 100)
                    .setReason(mapToStripeRefundReason(requestDto.getReason()))
                    .build();

            Refund stripeRefund = retryStripeCall(() -> Refund.create(params));

            com.adamo.vrspfab.payments.Refund refund = new com.adamo.vrspfab.payments.Refund();
            refund.setPayment(payment);
            refund.setRefundTransactionId(stripeRefund.getId());
            refund.setAmount(BigDecimal.valueOf(requestDto.getAmount()));
            refund.setCurrency(payment.getCurrency());
            refund.setReason(requestDto.getReason());
            refund.setStatus(mapStripeRefundStatus(stripeRefund.getStatus()));
            refund.setProcessedAt(LocalDateTime.now());
            refundRepository.save(refund);

            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);

            return new RefundResponseDto(refund.getId(), stripeRefund.getId(), refund.getStatus());

        } catch (StripeException e) {
            log.error("Stripe refund failed for payment ID {}: {}", payment.getId(), e.getMessage());
            throw new PaymentException("Stripe refund failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return "StripePaymentProvider";
    }

    private Payment createPendingPayment(SessionRequestDto requestDto) {
        // Here you would look up the Reservation entity from the reservationId
        // For now, we build a new Payment object.
        Payment payment = Payment.builder()
                .reservation(null) // reservationRepository.findById(requestDto.getReservationId()).orElseThrow(...)
                .amount(requestDto.getAmount())
                .currency(requestDto.getCurrency())
                .status(PaymentStatus.PENDING)
                .provider("stripePaymentProvider")
                .build();
        return paymentRepository.save(payment);
    }

    private RefundCreateParams.Reason mapToStripeRefundReason(String reason) {
        if (reason == null) return null;
        return switch (reason.toLowerCase()) {
            case "duplicate" -> RefundCreateParams.Reason.DUPLICATE;
            case "fraudulent" -> RefundCreateParams.Reason.FRAUDULENT;
            case "requested_by_customer" -> RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER;
            default -> null; // Let Stripe use its default
        };
    }

    private RefundStatus mapStripeRefundStatus(String status) {
        if (status == null) return RefundStatus.PENDING;
        return switch (status) {
            case "succeeded" -> RefundStatus.PROCESSED;
            case "pending" -> RefundStatus.PENDING;
            case "failed" -> RefundStatus.FAILED;
            case "requires_action" -> RefundStatus.PENDING;
            default -> RefundStatus.PENDING;
        };
    }

    private <T> T retryStripeCall(StripeOperation<T> operation) throws StripeException {
        int maxRetries = 3;
        int attempt = 0;
        StripeException lastException = null;

        while (attempt < maxRetries) {
            try {
                return operation.execute();
            } catch (StripeException e) {
                log.warn("Stripe API call failed on attempt {}/{}. Retrying...", attempt + 1, maxRetries, e);
                lastException = e;
                attempt++;
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(1000L * attempt); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        throw lastException;
    }

    @FunctionalInterface
    interface StripeOperation<T> {
        T execute() throws StripeException;
    }
}
