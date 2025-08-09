package com.adamo.vrspfab.reservations;

/**
 * Thrown when an operation is attempted on a reservation in an invalid state
 * (e.g., trying to cancel a completed reservation).
 */
public class InvalidReservationStatusException extends ReservationBusinessException {
    public InvalidReservationStatusException(String message) {
        super(message);
    }
}
