package com.adamo.vrspfab.vehicles.exceptions;

/**
 * Custom exception to be thrown when a vehicle is created or updated
 * with a license plate that is not unique.
 */
public class DuplicateLicensePlateException extends RuntimeException {
    public DuplicateLicensePlateException(String message) {
        super(message);
    }
}
