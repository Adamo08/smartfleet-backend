package com.adamo.vrspfab.vehicles;

/**
 * Custom exception to be thrown when vehicle data is invalid
 * (e.g., future year, implausible mileage).
 */
public class InvalidVehicleDataException extends RuntimeException {
    public InvalidVehicleDataException(String message) {
        super(message);
    }
}