package com.adamo.vrspfab.dashboard;

public enum ActivityType {
    USER_REGISTRATION("User Registration", "bg-purple-400"),
    USER_LOGIN("User Login", "bg-blue-400"),
    RESERVATION_CREATED("Reservation Created", "bg-green-400"),
    RESERVATION_CONFIRMED("Reservation Confirmed", "bg-green-500"),
    RESERVATION_CANCELLED("Reservation Cancelled", "bg-red-400"),
    PAYMENT_COMPLETED("Payment Completed", "bg-yellow-400"),
    PAYMENT_FAILED("Payment Failed", "bg-red-500"),
    REFUND_PROCESSED("Refund Processed", "bg-orange-400"),
    VEHICLE_ADDED("Vehicle Added", "bg-indigo-400"),
    VEHICLE_UPDATED("Vehicle Updated", "bg-blue-400"),
    VEHICLE_STATUS_CHANGED("Vehicle Status Changed", "bg-cyan-400"),
    ADMIN_ACTION("Admin Action", "bg-gray-400"),
    SYSTEM_EVENT("System Event", "bg-slate-400");
    
    private final String displayName;
    private final String iconColor;
    
    ActivityType(String displayName, String iconColor) {
        this.displayName = displayName;
        this.iconColor = iconColor;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getIconColor() {
        return iconColor;
    }
}
