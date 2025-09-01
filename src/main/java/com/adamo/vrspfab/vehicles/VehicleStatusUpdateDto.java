package com.adamo.vrspfab.vehicles;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VehicleStatusUpdateDto {
    
    @NotNull(message = "Vehicle status cannot be null")
    private VehicleStatus status;
}