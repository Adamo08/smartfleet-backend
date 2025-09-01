package com.adamo.vrspfab.slots;

/**
 * Enum representing different types of slots for vehicle rental.
 * This allows for flexible booking options from hourly to weekly rentals.
 */
public enum SlotType {
    HOURLY("Hourly rental - for short trips"),
    DAILY("Daily rental - for overnight trips"),
    WEEKLY("Weekly rental - for longer periods"),
    CUSTOM("Custom time range - for specific needs");

    private final String description;

    SlotType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

