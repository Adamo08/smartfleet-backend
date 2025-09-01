package com.adamo.vrspfab.vehicles.mappers;

import com.adamo.vrspfab.vehicles.Vehicle;
import com.adamo.vrspfab.vehicles.VehicleSummaryDto;
import com.adamo.vrspfab.vehicles.dto.CreateVehicleDto;
import com.adamo.vrspfab.vehicles.dto.UpdateVehicleDto;
import com.adamo.vrspfab.vehicles.dto.VehicleResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface EnhancedVehicleMapper {

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "brandId", source = "brand.id")
    @Mapping(target = "brandName", source = "brand.name")
    @Mapping(target = "modelId", source = "model.id")
    @Mapping(target = "modelName", source = "model.name")
    VehicleResponseDto toResponseDto(Vehicle vehicle);

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
    Vehicle toEntity(CreateVehicleDto createDto);

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
    void updateVehicleFromDto(UpdateVehicleDto updateDto, @MappingTarget Vehicle entity);

//    @Mapping(target = "id", source = "id")
//    @Mapping(target = "brand", source = "brand.name")
//    @Mapping(target = "model", source = "model.name")
//    @Mapping(target = "pricePerDay", source = "pricePerDay")
//    VehicleSummaryDto toSummaryDto(Vehicle vehicle);
}

