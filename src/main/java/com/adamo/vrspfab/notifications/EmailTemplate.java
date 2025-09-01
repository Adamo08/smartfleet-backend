package com.adamo.vrspfab.notifications;

import com.adamo.vrspfab.users.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "email_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;
    
    @Column(name = "subject", nullable = false, length = 200)
    private String subject;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "category", nullable = false, length = 50)
    private String category;
    
    @Column(name = "icon", length = 10)
    private String icon;
    
    @Column(name = "color", length = 20)
    private String color;
    
    @Column(name = "template_file", nullable = false, length = 100)
    private String templateFile; // References existing Thymeleaf template file
    
    @ElementCollection
    @CollectionTable(name = "email_template_variables", joinColumns = @JoinColumn(name = "template_id"))
    @Column(name = "variable_name")
    private List<String> variables;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    @Column(name = "usage_count")
    private Long usageCount = 0L;
    
    @Column(name = "last_modified", nullable = false)
    private LocalDateTime lastModified;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastModified = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastModified = LocalDateTime.now();
    }
    
    public void incrementUsageCount() {
        this.usageCount = (this.usageCount == null) ? 1L : this.usageCount + 1L;
    }
}
