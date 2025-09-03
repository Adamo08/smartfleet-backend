-- Create vehicle_categories table (only if it doesn't exist)
CREATE TABLE IF NOT EXISTS vehicle_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    icon_url VARCHAR(255) DEFAULT NULL
);

-- Create vehicle_brands table (only if it doesn't exist)
CREATE TABLE IF NOT EXISTS vehicle_brands (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    country_of_origin VARCHAR(255) DEFAULT NULL,
    logo_url VARCHAR(255) DEFAULT NULL
);

-- Create vehicle_models table (only if it doesn't exist)
CREATE TABLE IF NOT EXISTS vehicle_models (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    brand_id BIGINT NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (brand_id) REFERENCES vehicle_brands(id),
    UNIQUE KEY uk_model_brand (name, brand_id)
);

-- Create opening_hours table
CREATE TABLE opening_hours (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    day_of_week VARCHAR(20) NOT NULL,
    is_open BOOLEAN NOT NULL DEFAULT TRUE,
    open_time TIME,
    close_time TIME,
    is_24_hour BOOLEAN DEFAULT FALSE,
    notes TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_day_of_week (day_of_week)
);

-- Add new columns to vehicles table (only if they don't exist)
-- Note: MySQL doesn't support IF NOT EXISTS for ADD COLUMN, so we use a different approach
-- We'll add columns only if they don't exist by checking the information_schema

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'vehicles' 
     AND COLUMN_NAME = 'category_id') = 0,
    'ALTER TABLE vehicles ADD COLUMN category_id BIGINT',
    'SELECT "Column category_id already exists" as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'vehicles' 
     AND COLUMN_NAME = 'brand_id') = 0,
    'ALTER TABLE vehicles ADD COLUMN brand_id BIGINT',
    'SELECT "Column brand_id already exists" as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'vehicles' 
     AND COLUMN_NAME = 'model_id') = 0,
    'ALTER TABLE vehicles ADD COLUMN model_id BIGINT',
    'SELECT "Column model_id already exists" as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add foreign key constraints (only if they don't exist)
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'vehicles' 
     AND CONSTRAINT_NAME = 'fk_vehicles_category') = 0,
    'ALTER TABLE vehicles ADD CONSTRAINT fk_vehicles_category FOREIGN KEY (category_id) REFERENCES vehicle_categories(id)',
    'SELECT "Constraint fk_vehicles_category already exists" as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'vehicles' 
     AND CONSTRAINT_NAME = 'fk_vehicles_brand') = 0,
    'ALTER TABLE vehicles ADD CONSTRAINT fk_vehicles_brand FOREIGN KEY (brand_id) REFERENCES vehicle_brands(id)',
    'SELECT "Constraint fk_vehicles_brand already exists" as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'vehicles' 
     AND CONSTRAINT_NAME = 'fk_vehicles_model') = 0,
    'ALTER TABLE vehicles ADD CONSTRAINT fk_vehicles_model FOREIGN KEY (model_id) REFERENCES vehicle_models(id)',
    'SELECT "Constraint fk_vehicles_model already exists" as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Data insertion will be handled in V24 migration
