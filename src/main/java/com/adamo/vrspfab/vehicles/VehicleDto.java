package com.adamo.vrspfab.vehicles;

import com.adamo.vrspfab.validations.NotInFutureYear;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class VehicleDto {
    private Long id;

    // Entity relationships
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    private Long modelId;
    private String modelName;

    // Backward compatibility fields (for existing code)
    @NotBlank(message = "Brand cannot be blank")
    @Size(max = 50, message = "Brand must not exceed 50 characters")
    private String brand;

    @NotBlank(message = "Model cannot be blank")
    @Size(max = 50, message = "Model must not exceed 50 characters")
    private String model;

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
    @PositiveOrZero(message = "Mileage must be zero or positive")
    private Float mileage;

    @NotNull(message = "Price per day cannot be null")
    @Positive(message = "Price per day must be a positive value")
    @Max(value = 10000, message = "Price per day cannot exceed $10,000")
    private Double pricePerDay;

    private String imageUrl;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private LocalDate createdAt;

    private LocalDate updatedAt;
}