package com.adamo.vrspfab.vehicles.dto;

import com.adamo.vrspfab.vehicles.FuelType;
import com.adamo.vrspfab.vehicles.VehicleStatus;
import com.adamo.vrspfab.validations.NotInFutureYear;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateVehicleDto {
    
    private Long categoryId;
    
    private Long brandId;
    
    private Long modelId;
    
    @Positive(message = "Year must be a positive value")
    @Min(value = 1886, message = "Year must be no earlier than 1886")
    @NotInFutureYear
    private Integer year;
    
    @Pattern(regexp = "^[A-Z0-9-]{1,10}$", message = "License plate must be 1-10 alphanumeric characters or hyphens")
    private String licensePlate;
    
    private FuelType fuelType;
    
    private VehicleStatus status;
    
    @DecimalMin(value = "0.0", message = "Mileage must be zero or positive")
    @DecimalMax(value = "999999.9", message = "Mileage cannot exceed 999,999.9")
    private Float mileage;
    
    @DecimalMin(value = "0.01", message = "Price per day must be greater than zero")
    @DecimalMax(value = "10000.00", message = "Price per day cannot exceed $10,000")
    private Double pricePerDay;
    
    @Pattern(regexp = "^(https?://.*\\.(jpg|jpeg|png|gif|webp))?$", 
             message = "Image URL must be a valid HTTP/HTTPS URL pointing to an image")
    private String imageUrl;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}

