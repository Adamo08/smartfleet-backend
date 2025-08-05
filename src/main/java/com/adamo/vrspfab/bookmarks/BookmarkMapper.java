package com.adamo.vrspfab.bookmarks;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookmarkMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "reservationId", source = "reservation.id")
    BookmarkDto toDto(Bookmark bookmark);

    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "reservation.id", source = "reservationId")
    Bookmark toEntity(BookmarkDto bookmarkDTO);
}