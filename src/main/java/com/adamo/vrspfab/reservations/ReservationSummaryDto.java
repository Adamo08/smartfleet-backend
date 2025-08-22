package com.adamo.vrspfab.reservations;


import com.adamo.vrspfab.slots.SlotDto;
import com.adamo.vrspfab.users.UserSummaryDto;
import com.adamo.vrspfab.vehicles.VehicleSummaryDto;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * A summary Data Transfer Object for displaying a reservation in a list.
 * Provides key information in a lightweight format suitable for paginated views.
 */
@Data
public class ReservationSummaryDto {
    private Long id;
    private UserSummaryDto user;
    private VehicleSummaryDto vehicle;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private ReservationStatus status;
    private LocalDateTime createdAt;
}