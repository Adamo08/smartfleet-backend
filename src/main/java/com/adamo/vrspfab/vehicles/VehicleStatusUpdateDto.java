package com.adamo.vrspfab.vehicles;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VehicleStatusUpdateDto {
    @NotNull(message = "Status cannot be null")
    private VehicleStatus status;
}