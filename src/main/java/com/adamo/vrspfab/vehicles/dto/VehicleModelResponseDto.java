package com.adamo.vrspfab.vehicles.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VehicleModelResponseDto {
    private Long id;
    private String name;
    private Long brandId;
    private String brandName;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

