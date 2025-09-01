package com.adamo.vrspfab.vehicles.mappers;

import com.adamo.vrspfab.vehicles.VehicleModel;
import com.adamo.vrspfab.vehicles.dto.CreateVehicleModelDto;
import com.adamo.vrspfab.vehicles.dto.UpdateVehicleModelDto;
import com.adamo.vrspfab.vehicles.dto.VehicleModelResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface EnhancedVehicleModelMapper {
    
    @Mapping(target = "brandId", source = "brand.id")
    @Mapping(target = "brandName", source = "brand.name")
    VehicleModelResponseDto toResponseDto(VehicleModel entity);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "vehicles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    VehicleModel toEntity(CreateVehicleModelDto createDto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "vehicles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget VehicleModel entity, UpdateVehicleModelDto updateDto);
    
    List<VehicleModelResponseDto> toResponseDtoList(List<VehicleModel> entities);
}

