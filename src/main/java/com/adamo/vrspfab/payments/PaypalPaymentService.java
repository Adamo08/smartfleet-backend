package com.adamo.vrspfab.payments;


import com.paypal.api.payments.Payment;
import com.paypal.api.payments.Refunded;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RequiredArgsConstructor
@Service("paypalPaymentProvider")
public class PaypalPaymentProvider implements PaymentProvider {


    private final APIContext apiContext;

    private static final int MAX_RETRIES = 3;

    @Override
    public PaymentResponseDto processPayment(PaymentRequestDto requestDto) {
        try {
            Map<String, Object> paymentParams = new HashMap<>();
            paymentParams.put("intent", "sale");
            paymentParams.put("payer", Map.of("payment_method", "paypal"));
            paymentParams.put("transactions", List.of(Map.of(
                    "amount", Map.of("total", requestDto.getAmount(), "currency", requestDto.getCurrency()),
                    "description", "Reservation payment"
            )));
            Payment payment = retry(() -> Payment.create(apiContext, paymentParams), MAX_RETRIES);
            return new PaymentResponseDto(null, payment.getId(), payment.getState(), payment.getLinks().get(1).getHref());
        } catch (PayPalRESTException e) {
            throw new PaymentException("PayPal payment failed: " + e.getMessage());
        }
    }

    @Override
    public SessionResponseDto createPaymentSession(SessionRequestDto requestDto) {
        try {
            Map<String, Object> paymentParams = new HashMap<>();
            paymentParams.put("intent", "sale");
            paymentParams.put("payer", Map.of("payment_method", "paypal"));
            paymentParams.put("transactions", List.of(Map.of(
                    "amount", Map.of("total", requestDto.getAmount(), "currency", requestDto.getCurrency()),
                    "description", "Reservation checkout",
                    "custom", requestDto.getReservationId().toString()
            )));
            paymentParams.put("redirect_urls", Map.of(
                    "return_url", requestDto.getSuccessUrl(),
                    "cancel_url", requestDto.getCancelUrl()
            ));
            Payment payment = retry(() -> Payment.create(apiContext, paymentParams), MAX_RETRIES);
            return new SessionResponseDto(payment.getId(), payment.getLinks().get(1).getHref());
        } catch (PayPalRESTException e) {
            throw new PaymentException("PayPal session creation failed: " + e.getMessage());
        }
    }

    @Override
    public PaymentResponseDto getPaymentStatus(Long paymentId) {
        try {
            Payment payment = Payment.get(apiContext, paymentId.toString());
            return new PaymentResponseDto(paymentId, payment.getId(), payment.getState(), null);
        } catch (PayPalRESTException e) {
            throw new PaymentException("PayPal status retrieval failed: " + e.getMessage());
        }
    }

    @Override
    public RefundResponseDto processRefund(RefundRequestDto requestDto) {
        try {
            Payment payment = Payment.get(apiContext, requestDto.getPaymentId().toString());
            Refunded refund = payment.refund(Map.of(
                    "amount", Map.of("total", requestDto.getAmount(), "currency", "USD"),
                    "description", requestDto.getReason()
            ));
            return new RefundResponseDto(Long.parseLong(refund.getId()), refund.getState());
        } catch (PayPalRESTException e) {
            throw new PaymentException("PayPal refund failed: " + e.getMessage());
        }
    }

    @Override
    public DisputeResponseDto handleDispute(DisputeRequestDto requestDto) {
        // Placeholder: PayPal disputes require specific API calls
        throw new PaymentException("PayPal dispute handling not fully implemented");
    }

    private <T> T retry(PaypalOperation<T> operation, int maxRetries) throws PayPalRESTException {
        int attempts = 0;
        while (attempts < maxRetries) {
            try {
                return operation.execute();
            } catch (PayPalRESTException e) {
                attempts++;
                if (attempts >= maxRetries) throw e;
                try {
                    Thread.sleep(1000 * attempts);
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