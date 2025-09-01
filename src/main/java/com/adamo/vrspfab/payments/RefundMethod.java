package com.adamo.vrspfab.payments;

public enum RefundMethod {
    ORIGINAL_PAYMENT_METHOD("Original Payment Method", "Refund to the original payment method used"),
    PAYPAL("PayPal", "Refund to PayPal account"),
    ONSITE_CASH("On-site Cash Refund", "Refund in cash at our location"),
    ADMIN_REFUND("Admin Refund", "Manual refund processed by administrator");

    private final String displayName;
    private final String description;

    RefundMethod(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
