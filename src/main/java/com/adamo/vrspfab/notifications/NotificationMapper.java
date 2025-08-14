package com.adamo.vrspfab.notifications;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    @Mapping(target = "read", source = "read")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "message", source = "message")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "id", source = "id")
    NotificationDto toDto(Notification notification);
}