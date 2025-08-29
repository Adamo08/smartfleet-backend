package com.adamo.vrspfab.vehicles.mappers;

import com.adamo.vrspfab.vehicles.VehicleCategory;
import com.adamo.vrspfab.vehicles.dto.CreateVehicleCategoryDto;
import com.adamo.vrspfab.vehicles.dto.UpdateVehicleCategoryDto;
import com.adamo.vrspfab.vehicles.dto.VehicleCategoryResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface EnhancedVehicleCategoryMapper {
    
    VehicleCategoryResponseDto toResponseDto(VehicleCategory entity);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "vehicles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    VehicleCategory toEntity(CreateVehicleCategoryDto createDto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vehicles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget VehicleCategory entity, UpdateVehicleCategoryDto updateDto);
    
    List<VehicleCategoryResponseDto> toResponseDtoList(List<VehicleCategory> entities);
}

