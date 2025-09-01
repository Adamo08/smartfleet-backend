-- Migration: Change refund_method from VARCHAR(100) to ENUM in refunds table

ALTER TABLE refunds
    MODIFY COLUMN refund_method ENUM(
    'ORIGINAL_PAYMENT_METHOD',
    'PAYPAL',
    'ONSITE_CASH',
    'ADMIN_REFUND'
    ) NOT NULL;
