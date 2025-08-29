package com.adamo.vrspfab.vehicles.exceptions;

/**
 * Exception thrown when a vehicle is not found in the system.
 */
public class VehicleNotFoundException extends RuntimeException{
    public VehicleNotFoundException(Long vehicleId) {
        super("Vehicle with ID " + vehicleId + " not found.");
    }
}
