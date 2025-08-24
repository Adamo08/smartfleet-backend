package com.adamo.vrspfab.vehicles;

import lombok.Data;

@Data
public class VehicleBrandDto {
    private Long id;
    private String name;
    private String description;
    private String logoUrl;
    private String countryOfOrigin;
    private Boolean isActive;
}
