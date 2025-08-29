package com.adamo.vrspfab.vehicles;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDeletionImpact {
    private String entityName;
    private String entityType;
    private int vehiclesAffected;
    private int modelsAffected;
    private int activeReservations;
    private boolean canDelete;
}
