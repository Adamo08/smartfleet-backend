package com.adamo.vrspfab.dashboard;

import com.adamo.vrspfab.users.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService {
    
    private final ActivityRepository activityRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * Record a new activity
     */
    @Transactional
    public ActivityDto recordActivity(ActivityType type, String title, String description, 
                                    User user, String entityType, Long entityId, 
                                    Map<String, Object> metadata) {
        try {
            String metadataJson = metadata != null ? objectMapper.writeValueAsString(metadata) : null;
            
            Activity activity = Activity.builder()
                    .activityType(type)
                    .title(title)
                    .description(description)
                    .user(user)
                    .relatedEntityType(entityType)
                    .relatedEntityId(entityId)
                    .metadata(metadataJson)
                    .build();
            
            Activity saved = activityRepository.save(activity);
            log.info("Recorded activity: {} for user: {}", type, user != null ? user.getEmail() : "SYSTEM");
            
            return mapToDto(saved);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize activity metadata", e);
            throw new RuntimeException("Failed to record activity", e);
        }
    }
    
    /**
     * Record a system activity (no user)
     */
    @Transactional
    public ActivityDto recordSystemActivity(ActivityType type, String title, String description,
                                          String entityType, Long entityId, Map<String, Object> metadata) {
        return recordActivity(type, title, description, null, entityType, entityId, metadata);
    }
    
    /**
     * Get recent activities for dashboard
     */
    @Transactional(readOnly = true)
    public List<ActivityDto> getRecentActivities(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return activityRepository.findByOrderByCreatedAtDesc(pageable)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get activities by type
     */
    @Transactional(readOnly = true)
    public List<ActivityDto> getActivitiesByType(ActivityType type, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return activityRepository.findByActivityTypeOrderByCreatedAtDesc(type, pageable)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get activities within date range
     */
    @Transactional(readOnly = true)
    public List<ActivityDto> getActivitiesInRange(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return activityRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate, pageable)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get activity statistics for analytics
     */
    @Transactional(readOnly = true)
    public Map<ActivityType, Long> getActivityStats(LocalDateTime startDate, LocalDateTime endDate) {
        return java.util.Arrays.stream(ActivityType.values())
                .collect(Collectors.toMap(
                        type -> type,
                        type -> activityRepository.countByActivityTypeAndCreatedAtBetween(type, startDate, endDate)
                ));
    }
    
    private ActivityDto mapToDto(Activity activity) {
        return ActivityDto.builder()
                .id(activity.getId())
                .activityType(activity.getActivityType())
                .title(activity.getTitle())
                .description(activity.getDescription())
                .userName(activity.getUser() != null ? activity.getUser().getFullName() : "System")
                .userId(activity.getUser() != null ? activity.getUser().getId() : null)
                .relatedEntityType(activity.getRelatedEntityType())
                .relatedEntityId(activity.getRelatedEntityId())
                .metadata(activity.getMetadata())
                .createdAt(activity.getCreatedAt())
                .iconColor(activity.getActivityType().getIconColor())
                .displayName(activity.getActivityType().getDisplayName())
                .build();
    }
}
