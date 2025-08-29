package com.adamo.vrspfab.vehicles.exceptions;

import com.adamo.vrspfab.common.ResourceNotFoundException;

public class VehicleCategoryNotFoundException extends ResourceNotFoundException {
    
    public VehicleCategoryNotFoundException(Long id) {
        super("Vehicle category not found with ID: " + id, "VehicleCategory");
    }
    
    public VehicleCategoryNotFoundException(String name) {
        super("Vehicle category not found with name: " + name, "VehicleCategory");
    }
}

