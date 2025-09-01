package com.adamo.vrspfab.vehicles.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityResponseDto {
    private Long vehicleId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean isAvailable;
}

