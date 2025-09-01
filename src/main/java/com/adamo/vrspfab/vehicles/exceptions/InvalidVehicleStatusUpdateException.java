package com.adamo.vrspfab.vehicles.exceptions;

/**
 * Custom exception to be thrown when a vehicle's status update is
 * not allowed based on business rules.
 */
public class InvalidVehicleStatusUpdateException extends RuntimeException {
    public InvalidVehicleStatusUpdateException(String message) {
        super(message);
    }
}
