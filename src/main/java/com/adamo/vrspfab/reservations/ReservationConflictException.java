package com.adamo.vrspfab.reservations;



/**
 * Thrown when an attempt to create a reservation conflicts with an existing one
 * for the same vehicle and overlapping time period.
 */
public class ReservationConflictException extends ReservationBusinessException {
    public ReservationConflictException(String message) {
        super(message);
    }
}
