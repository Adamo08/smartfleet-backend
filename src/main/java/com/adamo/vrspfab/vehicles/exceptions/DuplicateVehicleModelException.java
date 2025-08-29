package com.adamo.vrspfab.vehicles.exceptions;

public class DuplicateVehicleModelException extends RuntimeException {
    
    public DuplicateVehicleModelException(String modelName, String brandName) {
        super("Vehicle model '" + modelName + "' already exists for brand '" + brandName + "'");
    }
}

