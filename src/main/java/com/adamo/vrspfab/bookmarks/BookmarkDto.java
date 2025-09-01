package com.adamo.vrspfab.bookmarks;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import com.adamo.vrspfab.reservations.ReservationStatus;

@Data
public class BookmarkDto {
    private Long id;

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Reservation ID cannot be null")
    private Long reservationId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Enriched fields for displaying bookmarks without extra lookups
    private String userName;
    private String userEmail;

    // Reservation details
    private LocalDateTime reservationStartDate;
    private LocalDateTime reservationEndDate;
    private ReservationStatus reservationStatus;

    // Vehicle details from the associated reservation
    private Long vehicleId;
    private String vehicleBrand;
    private String vehicleModel;
    private String vehicleImageUrl;
}
