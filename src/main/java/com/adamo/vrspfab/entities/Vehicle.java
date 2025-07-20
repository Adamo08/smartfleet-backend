package com.adamo.vrspfab.entities;


import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
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
    private String fuelType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleStatus status;


    @Column(name = "mileage")
    private float mileage;


    @OneToMany(mappedBy = "vehicle", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Slot> slots;

}
