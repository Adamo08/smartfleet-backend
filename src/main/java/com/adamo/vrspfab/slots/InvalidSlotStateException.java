package com.adamo.vrspfab.slots;

/**
 * Custom exception to be thrown when a slot operation is attempted
 * on a slot that is not in the expected state (e.g., trying to book an unavailable slot).
 */
public class InvalidSlotStateException extends RuntimeException {
    public InvalidSlotStateException(String message) {
        super(message);
    }
}
