package com.adamo.vrspfab.vehicles;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VehicleMapper {
    @Mapping(target = "vehicleType", source = "vehicleType")
    @Mapping(target = "fuelType", source = "fuelType")
    @Mapping(target = "status", source = "status")
    VehicleDto toDto(Vehicle vehicle);

    @Mapping(target = "vehicleType", source = "vehicleType")
    @Mapping(target = "fuelType", source = "fuelType")
    @Mapping(target = "status", source = "status")
    Vehicle toEntity(VehicleDto vehicleDTO);

    void updateVehicleFromDto(VehicleDto vehicleDto, @MappingTarget Vehicle vehicle);
}
