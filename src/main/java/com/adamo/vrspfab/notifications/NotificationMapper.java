package com.adamo.vrspfab.notifications;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    @Mapping(target = "read", source = "read")
    NotificationDto toDto(Notification notification);
}