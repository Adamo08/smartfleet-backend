package com.adamo.vrspfab.vehicles.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateVehicleModelDto {
    
    @Size(min = 1, max = 50, message = "Model name must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-&.]+$", message = "Model name contains invalid characters")
    private String name;
    
    private Long brandId;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    private Boolean isActive;
}

