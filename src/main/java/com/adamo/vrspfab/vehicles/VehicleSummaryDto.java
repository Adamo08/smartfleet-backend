package com.adamo.vrspfab.vehicles;


import lombok.Data;

@Data
public class VehicleSummaryDto {
    private Long id;
    private String brand;
    private String model;
    private String licensePlate;
    private Integer year;
    private String imageUrl;
}
