package com.adamo.vrspfab.vehicles;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO containing information about the impact of deactivating a vehicle entity
 * (category, brand, or model) on vehicles and reservations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleStatusInfo {
    
    /**
     * Total number of vehicles that exist for this entity
     */
    private int totalVehicles;
    
    /**
     * Number of vehicles that will be affected by deactivating this entity
     */
    private int affectedVehicles;
    
    /**
     * Whether there are any active reservations for vehicles of this entity
     */
    private boolean hasActiveReservations;
    
    /**
     * Number of future reservations that will be affected
     */
    private int futureReservations;
    
    /**
     * Number of currently active vehicles of this entity
     */
    private int activeVehicles;
    
    /**
     * Number of inactive vehicles of this entity
     */
    private int inactiveVehicles;
}
