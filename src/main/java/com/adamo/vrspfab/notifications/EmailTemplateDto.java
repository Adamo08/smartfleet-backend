package com.adamo.vrspfab.notifications;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplateDto {
    
    private Long id;
    private String name;
    private NotificationType type;
    private String subject;
    private String description;
    private String category;
    private String icon;
    private String color;
    private String templateFile; // References existing Thymeleaf template
    private List<String> variables;
    private boolean isActive;
    private Long usageCount;
    private LocalDateTime lastModified;
    private LocalDateTime createdAt;
    private Long createdById;
    private String createdByEmail;
}
