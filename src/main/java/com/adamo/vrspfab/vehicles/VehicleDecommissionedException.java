package com.adamo.vrspfab.vehicles;

/**
 * Custom exception to be thrown when an operation is attempted on a vehicle
 * that has been decommissioned or is otherwise unavailable.
 */
public class VehicleDecommissionedException extends RuntimeException {
    public VehicleDecommissionedException(String message) {
        super(message);
    }
}
