package com.adamo.vrspfab.slots;

/**
 * Custom exception to be thrown when slot start and end times are invalid
 * (e.g., start time is after end time) or when a new slot conflicts with existing ones.
 */
public class InvalidSlotTimeException extends RuntimeException {
    public InvalidSlotTimeException(String message) {
        super(message);
    }
}
