package com.adamo.vrspfab.vehicles.mappers;

import com.adamo.vrspfab.vehicles.VehicleModel;
import com.adamo.vrspfab.vehicles.VehicleModelDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VehicleModelMapper {
    
    @Mapping(target = "brandId", source = "brand.id")
    @Mapping(target = "brandName", source = "brand.name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "createdAt", source = "createdAt", dateFormat = "yyyy-MM-dd'T'HH:mm:ss")
    @Mapping(target = "updatedAt", source = "updatedAt", dateFormat = "yyyy-MM-dd'T'HH:mm:ss")
    VehicleModelDto toDto(VehicleModel entity);
    
    @Mapping(target = "vehicles", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    VehicleModel toEntity(VehicleModelDto dto);
    
    List<VehicleModelDto> toDtoList(List<VehicleModel> entities);
    
    List<VehicleModel> toEntityList(List<VehicleModelDto> dtos);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vehicles", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget VehicleModel entity, VehicleModelDto dto);
}
