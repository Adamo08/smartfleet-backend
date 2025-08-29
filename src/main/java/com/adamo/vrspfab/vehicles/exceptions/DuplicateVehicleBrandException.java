package com.adamo.vrspfab.vehicles.exceptions;

public class DuplicateVehicleBrandException extends RuntimeException {
    
    public DuplicateVehicleBrandException(String name) {
        super("Vehicle brand with name '" + name + "' already exists");
    }
}

