package com.adamo.vrspfab.vehicles.mappers;

import com.adamo.vrspfab.vehicles.VehicleCategory;
import com.adamo.vrspfab.vehicles.VehicleCategoryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VehicleCategoryMapper {
    
    @Mapping(target = "iconUrl", source = "iconUrl")
    @Mapping(target = "createdAt", source = "createdAt", dateFormat = "yyyy-MM-dd'T'HH:mm:ss")
    @Mapping(target = "updatedAt", source = "updatedAt", dateFormat = "yyyy-MM-dd'T'HH:mm:ss")
    VehicleCategoryDto toDto(VehicleCategory entity);
    
    @Mapping(target = "vehicles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    VehicleCategory toEntity(VehicleCategoryDto dto);
    
    List<VehicleCategoryDto> toDtoList(List<VehicleCategory> entities);
    
    List<VehicleCategory> toEntityList(List<VehicleCategoryDto> dtos);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vehicles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget VehicleCategory entity, VehicleCategoryDto dto);
}
