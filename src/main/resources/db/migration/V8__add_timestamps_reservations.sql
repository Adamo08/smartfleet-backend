-- V8__add_timestamps_reservations.sql

-- Since created_at and updated_at columns already exist in V0 migration,
-- we just need to modify them to have proper defaults

-- 1️⃣ Fill with current timestamp for existing rows (if any are NULL)
UPDATE reservations
SET created_at = NOW(),
    updated_at = NOW()
WHERE created_at IS NULL OR updated_at IS NULL;

-- 2️⃣ Modify columns to have proper defaults
ALTER TABLE reservations
    MODIFY created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    MODIFY updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6);


-- 4️⃣ Unique constraint to avoid duplicate reservations for same user/vehicle/start_date
ALTER TABLE reservations
    ADD CONSTRAINT uq_user_vehicle_start UNIQUE (user_id, vehicle_id, start_date);
