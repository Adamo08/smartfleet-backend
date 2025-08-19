package com.adamo.vrspfab.vehicles;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface VehicleMapper {

    VehicleDto toDto(Vehicle vehicle);

    @Mapping(target = "slots", ignore = true)
    @Mapping(target = "favorites", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    @Mapping(target = "testimonials", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Vehicle toEntity(VehicleDto vehicleDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slots", ignore = true)
    @Mapping(target = "favorites", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    @Mapping(target = "testimonials", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateVehicleFromDto(VehicleDto dto, @MappingTarget Vehicle entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "brand", source = "brand")
    @Mapping(target = "model", source = "model")
    @Mapping(target = "licensePlate", source = "licensePlate")
    @Mapping(target = "year", source = "year")
    @Mapping(target = "imageUrl", source = "imageUrl")
    VehicleSummaryDto toSummaryDto(Vehicle vehicle);
}