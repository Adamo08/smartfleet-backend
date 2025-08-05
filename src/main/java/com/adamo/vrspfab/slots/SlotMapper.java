package com.adamo.vrspfab.slots;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SlotMapper {
    @Mapping(target = "vehicleId", source = "vehicle.id")
    SlotDto toDto(Slot slot);

    @Mapping(target = "vehicle.id", source = "vehicleId")
    Slot toEntity(SlotDto slotDTO);
}