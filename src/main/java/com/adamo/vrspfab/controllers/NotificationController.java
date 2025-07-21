package com.adamo.vrspfab.controllers;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @GetMapping
    public String getNotifications() {
        return "List of notifications";
    }

    @PostMapping
    public String createNotification() {
        return "Notification created";
    }
}
