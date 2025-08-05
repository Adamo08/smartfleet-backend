package com.adamo.vrspfab.payments;

public interface PaymentProvider {

    /**
     * Processes a payment request.
     *
     * @param requestDto the payment request data transfer object
     * @return the payment response data transfer object
     */
    PaymentResponseDto processPayment(PaymentRequestDto requestDto);

    /**
     * Creates a payment session for a reservation.
     *
     * @param requestDto the session request data transfer object
     * @return the session response data transfer object
     */
    SessionResponseDto createPaymentSession(SessionRequestDto requestDto);

    /**
     * Retrieves the status of a payment.
     *
     * @param paymentId the ID of the payment
     * @return the payment response data transfer object containing the status
     */
    PaymentResponseDto getPaymentStatus(Long paymentId);

    /**
     * Processes a refund request.
     *
     * @param requestDto the refund request data transfer object
     * @return the refund response data transfer object
     */
    RefundResponseDto processRefund(RefundRequestDto requestDto);

    /**
     * Handles a dispute request.
     *
     * @param requestDto the dispute request data transfer object
     * @return the dispute response data transfer object
     */
    DisputeResponseDto handleDispute(DisputeRequestDto requestDto);
}
