package com.adamo.vrspfab.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDto {
    
    private Long id;
    private ActivityType activityType;
    private String title;
    private String description;
    private String userName;
    private Long userId;
    private String relatedEntityType;
    private Long relatedEntityId;
    private String metadata;
    private LocalDateTime createdAt;
    private String iconColor;
    private String displayName;
}
