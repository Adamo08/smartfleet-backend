package com.adamo.vrspfab.vehicles.mappers;

import com.adamo.vrspfab.vehicles.VehicleBrand;
import com.adamo.vrspfab.vehicles.dto.CreateVehicleBrandDto;
import com.adamo.vrspfab.vehicles.dto.UpdateVehicleBrandDto;
import com.adamo.vrspfab.vehicles.dto.VehicleBrandResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface EnhancedVehicleBrandMapper {
    
    VehicleBrandResponseDto toResponseDto(VehicleBrand entity);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "vehicles", ignore = true)
    @Mapping(target = "models", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    VehicleBrand toEntity(CreateVehicleBrandDto createDto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vehicles", ignore = true)
    @Mapping(target = "models", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget VehicleBrand entity, UpdateVehicleBrandDto updateDto);
    
    List<VehicleBrandResponseDto> toResponseDtoList(List<VehicleBrand> entities);
}

