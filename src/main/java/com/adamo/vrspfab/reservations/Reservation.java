package com.adamo.vrspfab.reservations;

import com.adamo.vrspfab.bookmarks.Bookmark;
import com.adamo.vrspfab.payments.Payment;
import com.adamo.vrspfab.slots.Slot;
import com.adamo.vrspfab.vehicles.Vehicle;
import com.adamo.vrspfab.users.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime; // Changed from java.sql.Timestamp
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(name = "comment", length = 500)
    private String comment;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "reservation")
    private Payment payment;

    @OneToOne
    @JoinColumn(name = "slot_id")
    private Slot slot;

    // Bidirectional relationship for Bookmarks
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Bookmark> bookmarks = new HashSet<>();
}