package com.adamo.vrspfab.notifications;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EnhancedBroadcastRequest {
    
    private String message;
    private NotificationType type;
    private String title;
    
    private BroadcastTarget target;
    private BroadcastSchedule schedule;
    private String priority;
    private boolean requiresConfirmation;
    private boolean trackAnalytics;
    
    @Data
    public static class BroadcastTarget {
        private String type; // "all", "role", "specific", "group"
        private String value; // role name, user IDs, group name
        private List<Long> userIds;
        private List<String> roles;
    }
    
    @Data
    public static class BroadcastSchedule {
        private boolean immediate;
        private LocalDateTime scheduledDate;
        private String timezone;
    }
}
