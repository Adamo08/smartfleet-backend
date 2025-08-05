package com.adamo.vrspfab.reservations;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
public class ReservationDto {
    private Long id;
    private Long userId;
    private Long vehicleId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String comment;
    private ReservationStatus status;
}
