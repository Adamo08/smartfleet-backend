package com.adamo.vrspfab.slots;

import jakarta.validation.constraints.FutureOrPresent; // Import for date validation
import jakarta.validation.constraints.NotNull; // Import for NotNull annotation
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.adamo.vrspfab.slots.SlotType;

@Setter
@Getter
public class SlotDto {
    private Long id;

    @NotNull(message = "Vehicle ID cannot be null")
    private Long vehicleId;

    @NotNull(message = "Start time cannot be null")
    @FutureOrPresent(message = "Start time must be in the present or future")
    private LocalDateTime startTime;

    @NotNull(message = "End time cannot be null")
    @FutureOrPresent(message = "End time must be in the present or future")
    private LocalDateTime endTime;

    private boolean available;
    
    private SlotType slotType;
    
    private BigDecimal price;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Enriched fields for displaying slot details without extra lookups
    private String vehicleBrand;
    private String vehicleModel;
}
