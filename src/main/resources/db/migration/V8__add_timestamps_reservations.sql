-- V8__add_timestamps_reservations.sql

-- 1️⃣ Add columns as nullable first
ALTER TABLE reservations
    ADD COLUMN created_at DATETIME(6) NULL,
    ADD COLUMN updated_at DATETIME(6) NULL;

-- 2️⃣ Fill with current timestamp for existing rows
UPDATE reservations
SET created_at = NOW(),
    updated_at = NOW()
WHERE created_at IS NULL OR updated_at IS NULL;

-- 3️⃣ Change to NOT NULL and set default for future inserts
ALTER TABLE reservations
    MODIFY created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    MODIFY updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6);


-- 4️⃣ Unique constraint to avoid duplicate reservations for same user/vehicle/start_date
ALTER TABLE reservations
    ADD CONSTRAINT uq_user_vehicle_start UNIQUE (user_id, vehicle_id, start_date);
