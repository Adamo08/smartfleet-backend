package com.adamo.vrspfab.slots;

import com.adamo.vrspfab.reservations.Reservation;
import com.adamo.vrspfab.vehicles.Vehicle;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "slots")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Slot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "is_available", nullable = false)
    private boolean available;

    @OneToOne(mappedBy = "slot")
    private Reservation reservation;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
