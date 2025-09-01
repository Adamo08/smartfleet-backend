package com.adamo.vrspfab.vehicles.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VehicleCategoryResponseDto {
    private Long id;
    private String name;
    private String description;
    private String iconUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

