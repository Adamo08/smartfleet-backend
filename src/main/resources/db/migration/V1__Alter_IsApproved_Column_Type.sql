-- V1__Alter_IsApproved_Column_Type.sql

-- This migration alters the 'is_approved' column in the 'testimonials' table.
-- It changes the column type from BIT(1) to TINYINT(1) to ensure boolean
-- values (true/false) are stored as 1/0 respectively, which is a common
-- and clear representation, and can influence how database clients display it.

ALTER TABLE testimonials
    MODIFY COLUMN is_approved TINYINT(1) NOT NULL DEFAULT 0;

-- Explanation:
-- - TINYINT(1): This is the standard MySQL type for boolean values, storing 0 or 1.
-- - NOT NULL: Ensures the column always has a value.
-- - DEFAULT 0: Sets a default value of 0 (false) for any new rows,
--              or for existing rows if the column was previously nullable
--              and had NULL values (though your entity specifies nullable = false).
