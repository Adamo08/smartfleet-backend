package com.adamo.vrspfab.reservations;


/**
 * Thrown when reservation dates are invalid (e.g., end date is before start date, or dates are in the past).
 */
public class InvalidReservationDateException extends ReservationBusinessException {
    public InvalidReservationDateException(String message) {
        super(message);
    }
}
