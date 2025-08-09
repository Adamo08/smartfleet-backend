package com.adamo.vrspfab.reservations;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Data Transfer Object for administrators to update a reservation's status.
 */
@Data
public class AdminReservationUpdateRequest {

    /**
     * The new status for the reservation.
     * Must not be null.
     */
    @NotNull(message = "Reservation status cannot be null.")
    private ReservationStatus status;
}