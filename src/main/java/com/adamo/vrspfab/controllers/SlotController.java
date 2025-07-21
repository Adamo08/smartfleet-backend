package com.adamo.vrspfab.controllers;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/slots")
public class SlotController {

    @GetMapping
    public String getSlots(){
        return "List Of slots";
    }

    @PostMapping
    public String createSlot(){
        return "Slot created";
    }
}
