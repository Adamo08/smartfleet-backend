package com.adamo.vrspfab.settings;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "opening_hours")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpeningHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "is_open", nullable = false)
    private Boolean isOpen;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    @Column(name = "is_24_hour")
    private Boolean is24Hour;

    @Column(name = "notes")
    private String notes;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
