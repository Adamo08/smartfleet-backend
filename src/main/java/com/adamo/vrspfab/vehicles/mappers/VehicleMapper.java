package com.adamo.vrspfab.vehicles.mappers;
import com.adamo.vrspfab.vehicles.Vehicle;
import com.adamo.vrspfab.vehicles.VehicleDto;
import com.adamo.vrspfab.vehicles.VehicleSummaryDto;
import com.adamo.vrspfab.vehicles.dto.VehicleResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface VehicleMapper {

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "brandId", source = "brand.id")
    @Mapping(target = "brandName", source = "brand.name")
    @Mapping(target = "modelId", source = "model.id")
    @Mapping(target = "modelName", source = "model.name")
    @Mapping(target = "brand", source = "brand.name")
    @Mapping(target = "model", source = "model.name")
    VehicleDto toDto(Vehicle vehicle);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "model", ignore = true)
    @Mapping(target = "slots", ignore = true)
    @Mapping(target = "favorites", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    @Mapping(target = "testimonials", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Vehicle toEntity(VehicleDto vehicleDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "model", ignore = true)
    @Mapping(target = "slots", ignore = true)
    @Mapping(target = "favorites", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    @Mapping(target = "testimonials", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateVehicleFromDto(VehicleDto dto, @MappingTarget Vehicle entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "brand", source = "brand.name")
    @Mapping(target = "model", source = "model.name")
    @Mapping(target = "licensePlate", source = "licensePlate")
    @Mapping(target = "year", source = "year")
    @Mapping(target = "imageUrl", source = "imageUrl")
    VehicleSummaryDto toSummaryDto(Vehicle vehicle);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "brandId", source = "brand.id")
    @Mapping(target = "brandName", source = "brand.name")
    @Mapping(target = "modelId", source = "model.id")
    @Mapping(target = "modelName", source = "model.name")
    VehicleResponseDto toResponseDto(Vehicle vehicle);
}