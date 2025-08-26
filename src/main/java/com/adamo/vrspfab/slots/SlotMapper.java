package com.adamo.vrspfab.slots;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants; // Import for MappingConstants.ComponentModel.SPRING

@Mapper(componentModel = "spring")
public interface SlotMapper {
    @Mapping(target = "vehicleId", source = "vehicle.id")
    @Mapping(target = "vehicleBrand", source = "vehicle.brand.name")
    @Mapping(target = "vehicleModel", source = "vehicle.model.name")
    SlotDto toDto(Slot slot);

    @Mapping(target = "vehicle", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Slot toEntity(SlotDto slotDTO);
}
