-- Add refunded_amount column to payments table (only if it doesn't exist)
-- Since refunded_amount already exists in V0 migration, we just need to ensure it has the right default
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'payments' 
     AND COLUMN_NAME = 'refunded_amount') = 0,
    'ALTER TABLE payments ADD COLUMN refunded_amount DECIMAL(10, 2) DEFAULT 0.00',
    'SELECT "Column refunded_amount already exists" as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Update any existing payment that has refunds to calculate the refunded amount
UPDATE payments 
SET refunded_amount = (
    SELECT COALESCE(SUM(r.amount), 0.00)
    FROM refunds r 
    WHERE r.payment_id = payments.id 
    AND r.status IN ('PROCESSED', 'PARTIALLY_PROCESSED')
)
WHERE id IN (
    SELECT DISTINCT payment_id 
    FROM refunds 
    WHERE status IN ('PROCESSED', 'PARTIALLY_PROCESSED')
);

-- Update payment status for payments that have been fully refunded
UPDATE payments 
SET status = 'REFUNDED'
WHERE refunded_amount >= amount 
AND status != 'REFUNDED';

-- Update payment status for payments that have been partially refunded
UPDATE payments 
SET status = 'PARTIALLY_REFUNDED'
WHERE refunded_amount > 0 
AND refunded_amount < amount 
AND status NOT IN ('REFUNDED', 'PARTIALLY_REFUNDED');
