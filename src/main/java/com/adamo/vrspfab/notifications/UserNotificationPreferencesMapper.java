package com.adamo.vrspfab.notifications;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserNotificationPreferencesMapper {
    UserNotificationPreferencesDto toDto(UserNotificationPreferences preferences);
    void updateFromDto(UserNotificationPreferencesDto dto, @MappingTarget UserNotificationPreferences preferences);
}
