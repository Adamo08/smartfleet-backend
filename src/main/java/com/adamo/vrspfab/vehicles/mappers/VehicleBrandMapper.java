package com.adamo.vrspfab.vehicles;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VehicleBrandMapper {
    
    @Mapping(target = "logoUrl", source = "logoUrl")
    @Mapping(target = "countryOfOrigin", source = "countryOfOrigin")
    VehicleBrandDto toDto(VehicleBrand entity);
    
    VehicleBrand toEntity(VehicleBrandDto dto);
    
    List<VehicleBrandDto> toDtoList(List<VehicleBrand> entities);
    
    List<VehicleBrand> toEntityList(List<VehicleBrandDto> dtos);
    
    @Mapping(target = "id", ignore = true)
    void updateEntity(@MappingTarget VehicleBrand entity, VehicleBrandDto dto);
}
