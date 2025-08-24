-- Create vehicle_categories table
CREATE TABLE vehicle_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create vehicle_brands table
CREATE TABLE vehicle_brands (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create vehicle_models table
CREATE TABLE vehicle_models (
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

-- Add new columns to vehicles table
ALTER TABLE vehicles 
ADD COLUMN category_id BIGINT,
ADD COLUMN brand_id BIGINT,
ADD COLUMN model_id BIGINT;

-- Add foreign key constraints
ALTER TABLE vehicles 
ADD CONSTRAINT fk_vehicles_category FOREIGN KEY (category_id) REFERENCES vehicle_categories(id),
ADD CONSTRAINT fk_vehicles_brand FOREIGN KEY (brand_id) REFERENCES vehicle_brands(id),
ADD CONSTRAINT fk_vehicles_model FOREIGN KEY (model_id) REFERENCES vehicle_models(id);

-- Insert some default data
INSERT INTO vehicle_categories (name, description) VALUES 
('Personal', 'Personal vehicles for individual use'),
('Commercial', 'Commercial vehicles for business use'),
('Luxury', 'High-end luxury vehicles'),
('Utility', 'Utility and work vehicles');

INSERT INTO vehicle_brands (name, description) VALUES 
('Toyota', 'Japanese automotive manufacturer'),
('Honda', 'Japanese automotive manufacturer'),
('Ford', 'American automotive manufacturer'),
('BMW', 'German luxury automotive manufacturer'),
('Mercedes-Benz', 'German luxury automotive manufacturer');

INSERT INTO vehicle_models (name, brand_id, description) VALUES 
('Camry', 1, 'Mid-size sedan'),
('Corolla', 1, 'Compact sedan'),
('Civic', 2, 'Compact sedan'),
('Accord', 2, 'Mid-size sedan'),
('F-150', 3, 'Full-size pickup truck'),
('3 Series', 4, 'Compact luxury sedan'),
('C-Class', 5, 'Compact luxury sedan');
