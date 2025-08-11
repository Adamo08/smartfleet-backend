--
-- Change the `reason` column in the `refunds` table to be nullable.
--

ALTER TABLE refunds
    MODIFY COLUMN reason VARCHAR(255) NULL;