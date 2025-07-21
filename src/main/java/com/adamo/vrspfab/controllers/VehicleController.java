package com.adamo.vrspfab.controllers;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vehicles")
public class VehicleController {

    @GetMapping
    public String getVehicles() {
        return "List of vehicles";
    }

    @PostMapping
    public String createVehicle() {
        return "Vehicle created";
    }
}
