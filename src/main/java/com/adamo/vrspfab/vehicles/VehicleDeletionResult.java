package com.adamo.vrspfab.vehicles;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDeletionResult {
    private boolean canDelete;
    private String reason;
    private String message;

    public static VehicleDeletionResult success(String message) {
        return VehicleDeletionResult.builder()
                .canDelete(true)
                .message(message)
                .build();
    }

    public static VehicleDeletionResult failure(String reason) {
        return VehicleDeletionResult.builder()
                .canDelete(false)
                .reason(reason)
                .build();
    }
}
