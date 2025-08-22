package com.adamo.vrspfab.notifications;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationFilter {
    private Long userId;
    private Boolean read;
    private NotificationType type;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
