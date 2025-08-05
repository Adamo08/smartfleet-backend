package com.adamo.vrspfab.payments;

import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service("paypalPaymentProvider")
public class PaypalPaymentProvider implements PaymentProvider {

    private final APIContext apiContext;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;

    private static final Set<String> VALID_CURRENCIES = Set.of("USD", "EUR", "GBP", "AUD", "CAD");

    @Override
    @Transactional
    public PaymentResponseDto processPayment(PaymentRequestDto requestDto) {
        validateRequest(requestDto.getAmount(), requestDto.getCurrency());
        try {
            Payment payment = paymentRepository.findByReservationId(requestDto.getReservationId())
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found for reservation ID: " + requestDto.getReservationId()));

            com.paypal.api.payments.Payment paypalPayment = createPaypalPayment(
                    requestDto.getAmount(),
                    requestDto.getCurrency(),
                    "Reservation payment for ID: " + requestDto.getReservationId(),
                    null,
                    null,
                    "sale");

            com.paypal.api.payments.Payment createdPayment = retry(() -> paypalPayment.create(apiContext));
            if (createdPayment == null || createdPayment.getId() == null) {
                throw new PaymentException("PayPal payment creation returned null");
            }

            payment.setTransactionId(createdPayment.getId());
            payment.setStatus(mapPaypalStatus(createdPayment.getState()));
            paymentRepository.save(payment);

            String approvalUrl = createdPayment.getLinks().stream()
                    .filter(link -> "approval_url".equals(link.getRel()))
                    .findFirst()
                    .map(Links::getHref)
                    .orElse(null);

            return new PaymentResponseDto(payment.getId(), createdPayment.getId(), createdPayment.getState(), approvalUrl);
        } catch (PayPalRESTException e) {
            throw new PaymentException("PayPal payment failed: " + e.getMessage());
        }
    }

    @Override
    public SessionResponseDto createPaymentSession(SessionRequestDto requestDto) {
        validateRequest(requestDto.getAmount(), requestDto.getCurrency());
        try {
            com.paypal.api.payments.Payment paypalPayment = createPaypalPayment(
                    requestDto.getAmount(),
                    requestDto.getCurrency(),
                    "Reservation checkout for ID: " + requestDto.getReservationId(),
                    requestDto.getSuccessUrl(),
                    requestDto.getCancelUrl(),
                    "sale");

            com.paypal.api.payments.Payment createdPayment = retry(() -> paypalPayment.create(apiContext));
            if (createdPayment == null || createdPayment.getId() == null) {
                throw new PaymentException("PayPal session creation returned null");
            }

            String sessionUrl = String.valueOf(createdPayment.getLinks().stream()
                    .filter(link -> "approval_url".equals(link.getRel()))
                    .findFirst()
                    .orElseThrow(() -> new PaymentException("No approval URL found")));

            return new SessionResponseDto(createdPayment.getId(), sessionUrl);
        } catch (PayPalRESTException e) {
            throw new PaymentException("PayPal session creation failed: " + e.getMessage());
        }
    }

    @Override
    public PaymentResponseDto getPaymentStatus(Long paymentId) {
        try {
            Payment payment = paymentRepository.findWithDetailsById(paymentId)
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
            com.paypal.api.payments.Payment paypalPayment = retry(() -> com.paypal.api.payments.Payment.get(apiContext, payment.getTransactionId()));
            if (paypalPayment == null) {
                throw new PaymentException("PayPal payment retrieval returned null");
            }
            payment.setStatus(mapPaypalStatus(paypalPayment.getState()));
            paymentRepository.save(payment);
            return new PaymentResponseDto(paymentId, paypalPayment.getId(), paypalPayment.getState(), null);
        } catch (PayPalRESTException e) {
            throw new PaymentException("PayPal status retrieval failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public RefundResponseDto processRefund(RefundRequestDto requestDto) {
        validateRequest(requestDto.getAmount(), null);
        try {
            Payment payment = paymentRepository.findWithDetailsById(requestDto.getPaymentId())
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + requestDto.getPaymentId()));
            Sale sale = retry(() -> Sale.get(apiContext, payment.getTransactionId()));
            if (sale == null) {
                throw new PaymentException("PayPal sale retrieval returned null");
            }
            RefundRequest refundRequest = new RefundRequest();
            Amount amount = new Amount();
            amount.setCurrency(payment.getCurrency());
            amount.setTotal(BigDecimal.valueOf(requestDto.getAmount()).setScale(2, RoundingMode.HALF_UP).toString());
            refundRequest.setAmount(amount);
            refundRequest.setDescription(requestDto.getReason());
            com.paypal.api.payments.Refund paypalRefund = retry(() -> sale.refund(apiContext, refundRequest));
            if (paypalRefund == null) {
                throw new PaymentException("PayPal refund returned null");
            }
            Refund refund = new Refund();
            refund.setPayment(payment);
            refund.setAmount(BigDecimal.valueOf(requestDto.getAmount()));
            refund.setCurrency(payment.getCurrency());
            refund.setReason(requestDto.getReason());
            refund.setStatus(RefundStatus.PROCESSED);
            refund.setProcessedAt(LocalDateTime.now());
            refund = refundRepository.save(refund);
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
            return new RefundResponseDto(refund.getId(), RefundStatus.valueOf(mapPaypalRefundStatus(paypalRefund.getState())));
        } catch (PayPalRESTException e) {
            throw new PaymentException("PayPal refund failed: " + e.getMessage());
        }
    }

    private void validateRequest(double amount, String currency) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (currency != null && !VALID_CURRENCIES.contains(currency.toUpperCase())) {
            throw new IllegalArgumentException("Invalid currency: " + currency + ". Must be one of " + VALID_CURRENCIES);
        }
    }

    private PaymentStatus mapPaypalStatus(String paypalStatus) {
        if (paypalStatus == null) {
            return PaymentStatus.PENDING;
        }
        return switch (paypalStatus.toLowerCase()) {
            case "completed" -> PaymentStatus.COMPLETED;
            case "failed" -> PaymentStatus.FAILED;
            default -> PaymentStatus.PENDING;
        };
    }

    private String mapPaypalRefundStatus(String paypalRefundState) {
        if (paypalRefundState == null) {
            return RefundStatus.PENDING.toString();
        }
        return switch (paypalRefundState.toLowerCase()) {
            case "completed" -> RefundStatus.PROCESSED.toString();
            case "pending" -> RefundStatus.PENDING.toString();
            case "failed" -> RefundStatus.FAILED.toString();
            default -> RefundStatus.PENDING.toString();
        };
    }

    private com.paypal.api.payments.Payment createPaypalPayment(double amount, String currency, String description, String successUrl, String cancelUrl, String intent) {
        Amount paypalAmount = new Amount();
        paypalAmount.setCurrency(currency);
        paypalAmount.setTotal(BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP).toString());

        Transaction transaction = new Transaction();
        transaction.setAmount(paypalAmount);
        transaction.setDescription(description);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        com.paypal.api.payments.Payment paypalPayment = new com.paypal.api.payments.Payment();
        paypalPayment.setIntent(intent);
        paypalPayment.setPayer(payer);
        paypalPayment.setTransactions(transactions);

        if (successUrl != null && cancelUrl != null) {
            RedirectUrls redirectUrls = new RedirectUrls();
            redirectUrls.setReturnUrl(successUrl);
            redirectUrls.setCancelUrl(cancelUrl);
            paypalPayment.setRedirectUrls(redirectUrls);
        }

        return paypalPayment;
    }

    private <T> T retry(PaypalOperation<T> operation) throws PayPalRESTException {
        int attempts = 0;
        while (attempts < 3) {
            try {
                return operation.execute();
            } catch (PayPalRESTException e) {
                attempts++;
                if (attempts >= 3) throw e;
                try {
                    Thread.sleep(1000L * attempts); // Exponential backoff
                } catch (InterruptedException ignored) {}
            }
        }
        throw new PayPalRESTException("Operation failed after retries");
    }

    @FunctionalInterface
    interface PaypalOperation<T> {
        T execute() throws PayPalRESTException;
    }
}