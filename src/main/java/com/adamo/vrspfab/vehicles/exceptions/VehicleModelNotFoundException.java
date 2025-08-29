package com.adamo.vrspfab.vehicles.exceptions;

import com.adamo.vrspfab.common.ResourceNotFoundException;

public class VehicleModelNotFoundException extends ResourceNotFoundException {
    
    public VehicleModelNotFoundException(Long id) {
        super("Vehicle model not found with ID: " + id, "VehicleModel");
    }
    
    public VehicleModelNotFoundException(String name, Long brandId) {
        super("Vehicle model '" + name + "' not found for brand ID: " + brandId, "VehicleModel");
    }
}

