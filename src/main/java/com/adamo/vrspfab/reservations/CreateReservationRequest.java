package com.adamo.vrspfab.reservations;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for creating a new reservation.
 * Ensures that clients provide only the necessary and valid data for a new booking.
 */
@Data
public class CreateReservationRequest {

    /**
     * The unique identifier of the vehicle to be reserved.
     * Must not be null.
     */
    @NotNull(message = "Vehicle ID cannot be null.")
    private Long vehicleId;


    /**
     * The unique identifier of the slot where the vehicle will be reserved.
     * Must not be null.
     */
    @NotNull(message = "Slot ID cannot be null.")
    private Long slotId;

    /**
     * The start date and time for the reservation.
     * Must not be null and must be in the future.
     */
    @NotNull(message = "Start date cannot be null.")
    @Future(message = "Start date must be in the future.")
    private LocalDateTime startDate;

    /**
     * The end date and time for the reservation.
     * Must not be null and must be in the future.
     */
    @NotNull(message = "End date cannot be null.")
    @Future(message = "End date must be in the future.")
    private LocalDateTime endDate;

    /**
     * An optional comment from the user regarding the reservation.
     * Limited to 500 characters.
     */
    @Size(max = 500, message = "Comment must not exceed 500 characters.")
    private String comment;
}