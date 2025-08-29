package com.adamo.vrspfab.dashboard;

import com.adamo.vrspfab.users.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "activities", indexes = {
    @Index(name = "idx_activity_created_at", columnList = "created_at"),
    @Index(name = "idx_activity_type", columnList = "activity_type"),
    @Index(name = "idx_activity_user_id", columnList = "user_id")
})
public class Activity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false)
    private ActivityType activityType;
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "description", nullable = false, length = 500)
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Can be null for system activities
    
    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType; // e.g., "RESERVATION", "PAYMENT", "VEHICLE"
    
    @Column(name = "related_entity_id")
    private Long relatedEntityId;
    
    @Column(name = "metadata", columnDefinition = "json")
    private String metadata; // Additional data as JSON
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
