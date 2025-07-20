package com.adamo.vrspfab.entities;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "testimonials")
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

    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @Column(name = "is_approved", nullable = false)
    private boolean isApproved;
}