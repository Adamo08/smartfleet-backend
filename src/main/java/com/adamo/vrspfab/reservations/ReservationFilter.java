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
}