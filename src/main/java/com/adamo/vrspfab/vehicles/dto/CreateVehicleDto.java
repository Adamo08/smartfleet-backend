package com.adamo.vrspfab.vehicles.dto;

import com.adamo.vrspfab.vehicles.FuelType;
import com.adamo.vrspfab.vehicles.VehicleStatus;
import com.adamo.vrspfab.validations.NotInFutureYear;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateVehicleDto {
    
    @NotNull(message = "Category is required")
    private Long categoryId;
    
    @NotNull(message = "Brand is required")
    private Long brandId;
    
    @NotNull(message = "Model is required")
    private Long modelId;
    
    @NotNull(message = "Year cannot be null")
    @Positive(message = "Year must be a positive value")
    @Min(value = 1886, message = "Year must be no earlier than 1886")
    @NotInFutureYear
    private Integer year;
    
    @NotBlank(message = "License plate cannot be blank")
    @Pattern(regexp = "^[A-Z0-9-]{1,10}$", message = "License plate must be 1-10 alphanumeric characters or hyphens")
    private String licensePlate;
    
    @NotNull(message = "Fuel type cannot be null")
    private FuelType fuelType;
    
    @NotNull(message = "Vehicle status cannot be null")
    private VehicleStatus status;
    
    @NotNull(message = "Mileage cannot be null")
    @DecimalMin(value = "0.0", message = "Mileage must be zero or positive")
    @DecimalMax(value = "999999.9", message = "Mileage cannot exceed 999,999.9")
    private Float mileage;
    
    @NotNull(message = "Price per day cannot be null")
    @DecimalMin(value = "0.01", message = "Price per day must be greater than zero")
    @DecimalMax(value = "10000.00", message = "Price per day cannot exceed $10,000")
    private Double pricePerDay;
    
    @Pattern(regexp = "^(https?://.*\\.(jpg|jpeg|png|gif|webp))?$", 
             message = "Image URL must be a valid HTTP/HTTPS URL pointing to an image")
    private String imageUrl;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}

