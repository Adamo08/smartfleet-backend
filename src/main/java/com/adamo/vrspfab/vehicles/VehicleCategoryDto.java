package com.adamo.vrspfab.vehicles;

import lombok.Data;

@Data
public class VehicleCategoryDto {
    private Long id;
    private String name;
    private String description;
    private String iconUrl;
    private Boolean isActive;
    private String createdAt;
    private String updatedAt;
}
