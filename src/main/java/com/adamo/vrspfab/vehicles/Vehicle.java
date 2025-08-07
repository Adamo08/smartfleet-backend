package com.adamo.vrspfab.vehicles;

import com.adamo.vrspfab.favorites.Favorite;
import com.adamo.vrspfab.reservations.Reservation;
import com.adamo.vrspfab.slots.Slot;
import com.adamo.vrspfab.testimonials.Testimonial;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "brand")
    private String brand;

    @Column(name = "model")
    private String model;

    @Column(name = "year")
    private int year;

    @Column(name = "license_plate", unique = true)
    private String licensePlate;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;

    @Column(name = "fuel_type")
    @Enumerated(EnumType.STRING)
    private FuelType fuelType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleStatus status;

    @Column(name = "mileage")
    private float mileage;

    @Column(name = "price_per_day")
    private Double pricePerDay;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "description", length = 1000)
    private String description;

    @OneToMany(mappedBy = "vehicle", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Slot> slots = new HashSet<>();

    // Bidirectional relationships
    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Favorite> favorites = new HashSet<>();

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Reservation> reservations = new HashSet<>();

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Testimonial> testimonials = new HashSet<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}