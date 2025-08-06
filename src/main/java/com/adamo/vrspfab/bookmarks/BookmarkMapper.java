package com.adamo.vrspfab.bookmarks;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BookmarkMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "reservationId", source = "reservation.id")
    @Mapping(target = "userName", expression = "java(bookmark.getUser().getFirstName() + \" \" + bookmark.getUser().getLastName())")
    @Mapping(target = "userEmail", source = "user.email")

    // Map Reservation details
    @Mapping(target = "reservationStartDate", source = "reservation.startDate")
    @Mapping(target = "reservationEndDate", source = "reservation.endDate")
    @Mapping(target = "reservationStatus", source = "reservation.status")

    // Map Vehicle details from Reservation
    @Mapping(target = "vehicleId", source = "reservation.vehicle.id")
    @Mapping(target = "vehicleBrand", source = "reservation.vehicle.brand")
    @Mapping(target = "vehicleModel", source = "reservation.vehicle.model")
    BookmarkDto toDto(Bookmark bookmark);

    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "reservation.id", source = "reservationId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Bookmark toEntity(BookmarkDto bookmarkDTO);
}
