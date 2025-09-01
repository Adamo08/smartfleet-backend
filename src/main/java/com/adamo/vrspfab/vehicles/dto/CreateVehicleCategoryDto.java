package com.adamo.vrspfab.vehicles.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateVehicleCategoryDto {
    
    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 50, message = "Category name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-&.]+$", message = "Category name contains invalid characters")
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @Pattern(regexp = "^(https?://.*\\.(jpg|jpeg|png|gif|webp|svg))?$", 
             message = "Icon URL must be a valid HTTP/HTTPS URL pointing to an image")
    private String iconUrl;
}

