package com.adamo.vrspfab.vehicles.exceptions;

/**
 * Custom exception to be thrown when a vehicle is not available for a requested operation
 * (e.g., reservation due to existing bookings or status).
 */
public class VehicleNotAvailableException extends RuntimeException {
    public VehicleNotAvailableException(String message) {
        super(message);
    }
}