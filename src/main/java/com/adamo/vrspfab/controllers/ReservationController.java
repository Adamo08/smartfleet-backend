package com.adamo.vrspfab.controllers;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    @GetMapping
    public String getReservations() {
        return "List of reservations";
    }

    @PostMapping
    public String createReservation() {
        return "Reservation created";
    }
}
