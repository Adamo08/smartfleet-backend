-- Update refund status enum to include REQUESTED status
-- First, update any existing PENDING refunds to REQUESTED if they haven't been processed
UPDATE refunds 
SET status = 'REQUESTED' 
WHERE status = 'PENDING' 
AND processed_at IS NULL;
