package com.adamo.vrspfab.slots;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class SlotDto {
    private Long id;
    private Long vehicleId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean isAvailable;
}
