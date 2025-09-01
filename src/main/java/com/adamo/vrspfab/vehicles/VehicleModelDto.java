package com.adamo.vrspfab.vehicles;

import lombok.Data;

@Data
public class VehicleModelDto {
    private Long id;
    private String name;
    private Long brandId;
    private String brandName;
    private String description;
    private Boolean isActive;
    private String createdAt;
    private String updatedAt;
}
