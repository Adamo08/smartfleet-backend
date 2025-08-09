package com.adamo.vrspfab.reservations;

/**
 * Base exception for all business logic errors related to reservations.
 */
public class ReservationBusinessException extends RuntimeException {
    public ReservationBusinessException(String message) {
        super(message);
    }
}
