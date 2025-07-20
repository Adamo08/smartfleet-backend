package com.adamo.vrspfab.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "favorites")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Favorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.sql.Timestamp createdAt;
}