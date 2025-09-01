package com.adamo.vrspfab.reservations;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Data Transfer Object for administrators to update a reservation's status and other fields.
 */
@Data
public class AdminReservationUpdateRequest {

    /**
     * The new status for the reservation.
     * Must not be null.
     */
    @NotNull(message = "Reservation status cannot be null.")
    private ReservationStatus status;

    /**
     * Optional comment for the reservation.
     */
    @Size(max = 500, message = "Comment cannot exceed 500 characters.")
    private String comment;

    /**
     * Optional admin notes (not stored in reservation, used for logging).
     */
    @Size(max = 1000, message = "Admin notes cannot exceed 1000 characters.")
    private String adminNotes;
}