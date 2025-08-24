package com.adamo.vrspfab.vehicles;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class VehicleFilter {
    private String search;
    private Long brandId;
    private Long modelId;
    private Long categoryId;
    private String fuelType;
    private String status;
    private Double minPrice;
    private Double maxPrice;
    private Integer minYear;
    private Integer maxYear;
    private Double minMileage;
    private Double maxMileage;
}
