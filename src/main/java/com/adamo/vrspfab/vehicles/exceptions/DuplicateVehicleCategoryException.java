package com.adamo.vrspfab.vehicles.exceptions;

public class DuplicateVehicleCategoryException extends RuntimeException {
    
    public DuplicateVehicleCategoryException(String name) {
        super("Vehicle category with name '" + name + "' already exists");
    }
}

