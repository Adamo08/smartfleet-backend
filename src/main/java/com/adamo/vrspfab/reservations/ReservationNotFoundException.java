package com.adamo.vrspfab.reservations;

import com.adamo.vrspfab.common.ResourceNotFoundException;

/**
 * Thrown when a reservation with a specific ID cannot be found.
 */
public class ReservationNotFoundException extends ResourceNotFoundException {
    public ReservationNotFoundException(Long id) {
        super("Reservation not found with ID: " + id, "Reservation");
    }
}