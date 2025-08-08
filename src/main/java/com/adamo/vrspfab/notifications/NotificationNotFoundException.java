package com.adamo.vrspfab.notifications;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotificationNotFoundException extends RuntimeException {
    public NotificationNotFoundException(Long id) {
        super("Notification not found with ID: " + id);
    }

    public NotificationNotFoundException(String message) {
        super(message);
    }
}