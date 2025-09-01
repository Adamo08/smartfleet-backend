package com.adamo.vrspfab.payments;

/**
 * Custom runtime exception for handling all payment-related errors in a consistent way.
 * This allows for specific exception handling at the controller level for payment operations.
 */
public class PaymentException extends RuntimeException {
    public PaymentException(String message) {
        super(message);
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
