package com.adamo.vrspfab.reservations;


import com.adamo.vrspfab.users.UserDto;
import com.adamo.vrspfab.vehicles.VehicleSummaryDto;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservationInfoForVehicleDto {
    private Long id;

    private UserDto user;
    private VehicleSummaryDto vehicle;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String comment;
    private ReservationStatus status;
}
