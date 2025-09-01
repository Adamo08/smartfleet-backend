package com.adamo.vrspfab.vehicles.mappers;

import com.adamo.vrspfab.vehicles.VehicleBrand;
import com.adamo.vrspfab.vehicles.VehicleBrandDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VehicleBrandMapper {
    
    @Mapping(target = "logoUrl", source = "logoUrl")
    @Mapping(target = "countryOfOrigin", source = "countryOfOrigin")
    VehicleBrandDto toDto(VehicleBrand entity);
    
    @Mapping(target = "vehicles", ignore = true)
    @Mapping(target = "models", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    VehicleBrand toEntity(VehicleBrandDto dto);
    
    List<VehicleBrandDto> toDtoList(List<VehicleBrand> entities);
    
    List<VehicleBrand> toEntityList(List<VehicleBrandDto> dtos);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vehicles", ignore = true)
    @Mapping(target = "models", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget VehicleBrand entity, VehicleBrandDto dto);
}
