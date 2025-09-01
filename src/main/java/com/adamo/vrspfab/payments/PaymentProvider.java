package com.adamo.vrspfab.payments;

/**
 * Defines the contract for all payment provider implementations (e.g., Stripe, PayPal).
 * This interface ensures that the business logic can interact with any payment gateway
 * through a consistent and standardized set of operations.
 */
public interface PaymentProvider {

    /**
     * Processes a direct payment using a specific payment method.
     * Best for scenarios where the payment details are collected directly on the frontend.
     *
     * @param requestDto The DTO containing payment details like amount, currency, and paymentMethodId.
     * @return A DTO with the result of the payment transaction.
     */
    PaymentResponseDto processPayment(PaymentRequestDto requestDto);

    /**
     * Creates a hosted checkout session. The user will be redirected to the provider's
     * secure page to complete the payment.
     *
     * @param requestDto The DTO containing session details like amount, currency, and success/cancel URLs.
     * @return A DTO containing the session ID and the URL for redirection.
     */
    SessionResponseDto createPaymentSession(SessionRequestDto requestDto);

    /**
     * Retrieves the latest status of a payment from the provider.
     * This is useful for manual checks but should be supplemented with webhooks for real-time updates.
     *
     * @param paymentId The internal database ID of the payment.
     * @return A DTO containing the latest status of the payment.
     */
    PaymentResponseDto getPaymentStatus(Long paymentId);

    /**
     * Processes a refund for a previously completed payment.
     *
     * @param requestDto The DTO containing the payment ID, amount, and reason for the refund.
     * @return A DTO with the result of the refund transaction.
     */
    RefundResponseDto processRefund(RefundRequestDto requestDto);

    /**
     * Check if a payment can be processed for a reservation
     */
    boolean canProcessPayment(Long reservationId);

    /**
     * Returns the name of the payment provider.
     * This is useful for logging, analytics, and debugging purposes.
     *
     * @return The name of the payment provider.
     */
    String getProviderName();
}
