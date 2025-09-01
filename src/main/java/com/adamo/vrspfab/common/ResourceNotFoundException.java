package com.adamo.vrspfab.common;


import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {
    private final String resourceType;

    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceType = "Resource";
    }

    public ResourceNotFoundException(String message, String resourceType) {
        super(message);
        this.resourceType = resourceType;
    }
}
