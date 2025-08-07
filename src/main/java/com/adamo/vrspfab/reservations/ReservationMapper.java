package com.adamo.vrspfab.reservations;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "vehicleId", source = "vehicle.id")
    ReservationDto toDto(Reservation reservation);

    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "vehicle.id", source = "vehicleId")
    Reservation toEntity(ReservationDto reservationDto);

    @Mapping(target = "user", source = "user")
    @Mapping(target = "vehicle", source = "vehicle")
    ReservationInfoForVehicleDto toReservationInfoDto(Reservation reservation);
}