package com.adamo.vrspfab.vehicles;


import lombok.Data;

@Data
public class VehicleDto {
    private Long id;
    private String brand;
    private String model;
    private Integer year;
    private String licensePlate;
    private VehicleType vehicleType;
    private FuelType fuelType;
    private VehicleStatus status;
    private Float mileage;
}
