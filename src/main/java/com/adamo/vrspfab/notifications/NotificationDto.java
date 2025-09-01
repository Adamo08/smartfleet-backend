package com.adamo.vrspfab.notifications;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationDto {
    private Long id;
    private Long userId;
    private String message;
    private NotificationType type;
    private LocalDateTime createdAt;
    private boolean read;
}