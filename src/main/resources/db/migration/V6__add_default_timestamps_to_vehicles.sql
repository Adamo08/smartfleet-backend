-- Set default values for existing NULLs
UPDATE `vehicles` SET `created_at` = NOW(6) WHERE `created_at` IS NULL;
UPDATE `vehicles` SET `updated_at` = NOW(6) WHERE `updated_at` IS NULL;

-- Then alter the columns
ALTER TABLE `vehicles`
    MODIFY COLUMN `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    MODIFY COLUMN `updated_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6);