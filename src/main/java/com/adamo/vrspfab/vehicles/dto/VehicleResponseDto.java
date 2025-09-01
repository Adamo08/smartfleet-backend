package com.adamo.vrspfab.vehicles.dto;

import com.adamo.vrspfab.vehicles.FuelType;
import com.adamo.vrspfab.vehicles.VehicleStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VehicleResponseDto {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    private Long modelId;
    private String modelName;
    private Integer year;
    private String licensePlate;
    private FuelType fuelType;
    private VehicleStatus status;
    private Float mileage;
    private Double pricePerDay;
    private String imageUrl;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

