package com.adamo.vrspfab.notifications;

import lombok.Data;

@Data
public class UserNotificationPreferencesDto {
    private boolean realTimeEnabled;
    private boolean emailEnabled;
}