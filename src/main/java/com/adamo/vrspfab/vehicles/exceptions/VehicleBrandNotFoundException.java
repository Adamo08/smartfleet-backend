package com.adamo.vrspfab.vehicles.exceptions;

import com.adamo.vrspfab.common.ResourceNotFoundException;

public class VehicleBrandNotFoundException extends ResourceNotFoundException {
    
    public VehicleBrandNotFoundException(Long id) {
        super("Vehicle brand not found with ID: " + id, "VehicleBrand");
    }
    
    public VehicleBrandNotFoundException(String name) {
        super("Vehicle brand not found with name: " + name, "VehicleBrand");
    }
}

