package com.adamo.vrspfab.vehicles.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VehicleBrandResponseDto {
    private Long id;
    private String name;
    private String description;
    private String logoUrl;
    private String countryOfOrigin;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

