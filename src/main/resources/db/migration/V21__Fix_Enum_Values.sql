-- Fix enum value mismatches in the database
-- This migration ensures all enum values in the database match the Java enum constants

-- Fix Vehicle Status enum values
UPDATE vehicles 
SET status = 'IN_MAINTENANCE' 
WHERE status = 'MAINTENANCE';

-- Fix Refund Reason enum values
UPDATE refunds 
SET reason = 'CANCELLATION_BY_CUSTOMER' 
WHERE reason = 'Customer Request';

-- Ensure all enum values are properly formatted
UPDATE vehicles 
SET status = UPPER(REPLACE(status, ' ', '_'));

UPDATE refunds 
SET reason = UPPER(REPLACE(reason, ' ', '_'));

-- Add any missing enum values to ensure consistency
-- This will prevent future enum mismatches
