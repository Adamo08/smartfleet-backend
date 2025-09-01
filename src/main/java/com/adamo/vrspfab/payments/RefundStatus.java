package com.adamo.vrspfab.payments;

public enum RefundStatus {
    REQUESTED,      // User has requested a refund, waiting for admin approval
    PENDING,        // Admin approved, refund is being processed
    PROCESSED,      // Refund completed successfully
    PARTIALLY_PROCESSED, // Partial refund completed
    FAILED,         // Refund processing failed
    DECLINED        // Admin declined the refund request
}