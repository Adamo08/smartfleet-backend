package com.adamo.vrspfab.payments;

public enum RefundReason {
    VEHICLE_UNAVAILABLE("Vehicle became unavailable"),
    CANCELLATION_BY_CUSTOMER("Customer cancelled reservation"),
    TECHNICAL_ISSUE("Technical issue with booking"),
    DUPLICATE_PAYMENT("Duplicate payment made"),
    WRONG_AMOUNT("Incorrect amount charged"),
    SERVICE_NOT_PROVIDED("Service not provided as expected"),
    EMERGENCY_CANCELLATION("Emergency cancellation"),
    WEATHER_CONDITIONS("Weather conditions prevented service"),
    VEHICLE_DAMAGE("Vehicle damage before rental"),
    OTHER("Other reason");

    private final String description;

    RefundReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
