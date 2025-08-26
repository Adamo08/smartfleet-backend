package com.adamo.vrspfab.slots;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoAvailableSlotsException extends RuntimeException {
    public NoAvailableSlotsException(String message) {
        super(message);
    }

    public NoAvailableSlotsException(String message, Throwable cause) {
        super(message, cause);
    }
}
