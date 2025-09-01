package com.adamo.vrspfab.reservations;

import com.adamo.vrspfab.users.UserMapper;
import com.adamo.vrspfab.vehicles.mappers.VehicleMapper;
import com.adamo.vrspfab.slots.SlotMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = {
                UserMapper.class,
                VehicleMapper.class,
                SlotMapper.class
        }
)
public interface ReservationMapper {

    /**
     * Maps a Reservation entity to its detailed DTO representation.
     *
     * @param reservation The source entity.
     * @return The detailed DTO.
     */
    @Mapping(target = "user", source = "user")
    @Mapping(target = "vehicle", source = "vehicle")
    @Mapping(target = "slots", source = "slots")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "bookingContext", source = "bookingContext")
    DetailedReservationDto toDetailedDto(Reservation reservation);

    /**
     * Maps a Reservation entity to its summary DTO representation.
     *
     * @param reservation The source entity.
     * @return The summary DTO.
     */
    @Mapping(target = "user", source = "user")
    @Mapping(target = "vehicle", source = "vehicle")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "bookingContext", source = "bookingContext")
    ReservationSummaryDto toSummaryDto(Reservation reservation);

    /**
     * Maps a creation request DTO to a Reservation entity.
     * Note: User and Vehicle are not mapped here and must be set manually in the service.
     *
     * @param request The source creation DTO.
     * @return The Reservation entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "vehicle", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "payment", ignore = true)
    @Mapping(target = "slots", ignore = true)
    @Mapping(target = "bookmarks", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "bookingContext", source = "bookingContext")
    Reservation fromCreateRequest(CreateReservationRequest request);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "vehicleId", source = "vehicle.id")
    ReservationDto toDto(Reservation reservation);

    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "vehicle.id", source = "vehicleId")
    @Mapping(target = "payment", ignore = true)
    @Mapping(target = "slots", ignore = true)
    @Mapping(target = "bookmarks", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Reservation toEntity(ReservationDto reservationDto);

    @Mapping(target = "user", source = "user")
    @Mapping(target = "vehicle", source = "vehicle")
    ReservationInfoForVehicleDto toReservationInfoDto(Reservation reservation);
}