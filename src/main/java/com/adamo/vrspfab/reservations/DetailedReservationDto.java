package com.adamo.vrspfab.reservations;

import com.adamo.vrspfab.slots.SlotDto;
import com.adamo.vrspfab.users.UserDto;
import com.adamo.vrspfab.vehicles.VehicleDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * A detailed Data Transfer Object representing a single Reservation.
 * It includes comprehensive information about the user and the vehicle involved.
 */
@Data
public class DetailedReservationDto {
    private Long id;
    private UserDto user;
    private VehicleDto vehicle;
    private Set<SlotDto> slots;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String comment;
    private ReservationStatus status;
    private LocalDateTime createdAt;
}