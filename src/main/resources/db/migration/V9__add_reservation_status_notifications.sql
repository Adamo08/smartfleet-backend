--
-- Add new notification types for reservation status changes
--

ALTER TABLE notifications
    MODIFY COLUMN type ENUM(
        'ACCOUNT_VERIFICATION',
        'BOOKING_CANCELLATION',
        'BOOKING_CONFIRMATION',
        'EVENT_REMINDER',
        'FEATURE_UPDATE',
        'FEEDBACK_REQUEST',
        'GENERAL_UPDATE',
        'MAINTENANCE_NOTIFICATION',
        'NEWSLETTER',
        'PASSWORD_RESET',
        'PAYMENT_FAILURE',
        'PAYMENT_SUCCESS',
        'PROMOTION_OFFER',
        'REFUND_ISSUED',
        'SECURITY_ALERT',
        'SLOT_AVAILABLE',
        'SLOT_UNAVAILABLE',
        'SURVEY_INVITATION',
        'SYSTEM_ALERT',
        'USER_MESSAGE',
        'RESERVATION_PENDING',
        'RESERVATION_CANCELLED',
        'RESERVATION_CONFIRMED'
) NOT NULL;
