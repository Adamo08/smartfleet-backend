package com.adamo.vrspfab.notifications;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminNotificationRequest {
    @NotBlank
    @Size(max = 500)
    private String message;

    @NotNull
    private NotificationType type;
}