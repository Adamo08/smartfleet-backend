-- Step 1: Update NULL values to 'LOCAL' before altering the column
UPDATE users SET auth_provider = 'LOCAL' WHERE auth_provider IS NULL;

-- Step 2: Alter the column to ENUM and set default
ALTER TABLE users
    MODIFY COLUMN auth_provider ENUM('LOCAL', 'FACEBOOK', 'GOOGLE') NOT NULL DEFAULT 'LOCAL';
