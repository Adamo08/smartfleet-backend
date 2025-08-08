--
-- Change the type of the is_read column to TINYINT(1) and set a default value of 0
--

ALTER TABLE `notifications`
    MODIFY COLUMN is_read TINYINT(1) NOT NULL DEFAULT 0;


--
-- Add default timestamps to the notifications table
--

ALTER TABLE `notifications`
    MODIFY COLUMN `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);
