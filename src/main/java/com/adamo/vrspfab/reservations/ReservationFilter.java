package com.adamo.vrspfab.reservations;


import lombok.Builder;
import lombok.Data;

/**
 * DTO to hold filtering criteria for reservation queries.
 */
@Data
@Builder
public class ReservationFilter {
    private Long userId;
    private Long vehicleId;
    private ReservationStatus status;
    private String searchTerm;
    private java.time.LocalDateTime startDate;
    private java.time.LocalDateTime endDate;
}