package com.adamo.vrspfab.notifications;

import com.adamo.vrspfab.users.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "broadcast_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BroadcastHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "title", length = 200)
    private String title;
    
    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;
    
    @Column(name = "target_type", nullable = false)
    private String targetType;
    
    @Column(name = "target_value", length = 500)
    private String targetValue;
    
    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BroadcastStatus status;
    
    @Column(name = "sent_count")
    private Long sentCount = 0L;
    
    @Column(name = "delivered_count")
    private Long deliveredCount = 0L;
    
    @Column(name = "read_count")
    private Long readCount = 0L;
    
    @Column(name = "click_count")
    private Long clickCount = 0L;
    
    @Column(name = "priority", length = 20)
    private String priority;
    
    @Column(name = "requires_confirmation")
    private boolean requiresConfirmation;
    
    @Column(name = "track_analytics")
    private boolean trackAnalytics;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    public enum BroadcastStatus {
        SCHEDULED, SENT, FAILED
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (scheduledAt == null) {
            scheduledAt = LocalDateTime.now();
        }
    }
}
