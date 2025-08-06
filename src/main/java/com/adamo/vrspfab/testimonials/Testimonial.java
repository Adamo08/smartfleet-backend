package com.adamo.vrspfab.testimonials;

import com.adamo.vrspfab.users.User;
import com.adamo.vrspfab.vehicles.Vehicle;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
        name = "testimonials",
        uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "vehicle_id"})
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Testimonial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    @Column(name = "rating", nullable = false)
    private int rating;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_approved", nullable = false)
    private boolean approved;

    @Column(name = "admin_reply_content", length = 1000)
    private String adminReplyContent;
}
