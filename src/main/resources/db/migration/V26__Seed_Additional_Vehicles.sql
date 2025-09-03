-- V26 Migration: Additional vehicles data
-- Generated on 2025-01-27
-- Additional 100+ vehicles to complement V25

INSERT INTO vehicles (license_plate, brand_id, model_id, category_id, year, fuel_type, mileage, price_per_day, status, description, image_url) VALUES

-- Additional Toyota Vehicles (Brand ID: 1)
('TOY-015', 1, 1, 1, 2021, 'PETROL', 35000, 42.00, 'AVAILABLE', 'Reliable Toyota Corolla with proven track record and low maintenance costs.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('TOY-016', 1, 2, 2, 2021, 'PETROL', 40000, 60.00, 'AVAILABLE', 'Well-maintained Toyota Camry with comfortable ride and spacious interior.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('TOY-017', 1, 3, 3, 2021, 'PETROL', 32000, 70.00, 'AVAILABLE', 'Toyota RAV4 with excellent reliability and resale value.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('TOY-018', 1, 4, 4, 2021, 'DIESEL', 45000, 80.00, 'AVAILABLE', 'Durable Toyota Hilux with exceptional off-road capability and towing power.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('TOY-019', 1, 5, 2, 2021, 'PETROL', 25000, 110.00, 'AVAILABLE', 'Toyota Land Cruiser with legendary reliability and luxury features.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),

-- Additional Volkswagen Vehicles (Brand ID: 2)
('VW-015', 2, 6, 1, 2021, 'PETROL', 38000, 45.00, 'AVAILABLE', 'Volkswagen Golf with German engineering and excellent build quality.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('VW-016', 2, 7, 2, 2021, 'DIESEL', 42000, 65.00, 'AVAILABLE', 'Volkswagen Passat with comfortable ride and efficient diesel engine.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('VW-017', 2, 8, 3, 2021, 'PETROL', 35000, 75.00, 'AVAILABLE', 'Volkswagen Tiguan with modern styling and practical interior space.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('VW-018', 2, 9, 1, 2021, 'PETROL', 30000, 38.00, 'AVAILABLE', 'Compact Volkswagen Polo perfect for urban driving and parking.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('VW-019', 2, 10, 2, 2021, 'DIESEL', 40000, 90.00, 'AVAILABLE', 'Volkswagen Touareg with premium features and excellent towing capacity.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),

-- Additional BMW Vehicles (Brand ID: 3)
('BMW-015', 3, 11, 7, 2021, 'PETROL', 28000, 110.00, 'AVAILABLE', 'BMW 3 Series with sporty handling and premium interior quality.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('BMW-016', 3, 12, 7, 2021, 'PETROL', 35000, 140.00, 'AVAILABLE', 'BMW 5 Series with executive comfort and advanced technology.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('BMW-017', 3, 13, 3, 2021, 'PETROL', 30000, 170.00, 'AVAILABLE', 'BMW X5 with luxury features and powerful performance.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('BMW-018', 3, 14, 8, 2021, 'HYBRID', 15000, 240.00, 'AVAILABLE', 'BMW i8 with futuristic design and hybrid performance technology.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('BMW-019', 3, 15, 7, 2021, 'PETROL', 20000, 210.00, 'AVAILABLE', 'BMW 7 Series with flagship luxury and advanced comfort features.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),

-- Additional Ford Vehicles (Brand ID: 4)
('FORD-017', 4, 16, 4, 2021, 'PETROL', 50000, 80.00, 'AVAILABLE', 'Ford F-150 with proven reliability and excellent towing capability.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('FORD-018', 4, 16, 4, 2021, 'DIESEL', 55000, 85.00, 'AVAILABLE', 'Ford F-150 Power Stroke with exceptional torque and fuel efficiency.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('FORD-019', 4, 17, 8, 2021, 'PETROL', 25000, 105.00, 'AVAILABLE', 'Ford Mustang with classic American muscle car performance.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('FORD-020', 4, 18, 3, 2021, 'PETROL', 35000, 85.00, 'AVAILABLE', 'Ford Explorer with family-friendly features and spacious interior.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('FORD-021', 4, 19, 3, 2021, 'PETROL', 30000, 70.00, 'AVAILABLE', 'Ford Escape with compact SUV practicality and fuel efficiency.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('FORD-022', 4, 20, 1, 2021, 'PETROL', 40000, 50.00, 'AVAILABLE', 'Ford Focus with excellent handling and reliable performance.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),

-- Additional Honda Vehicles (Brand ID: 5)
('HONDA-016', 5, 21, 1, 2021, 'PETROL', 40000, 45.00, 'AVAILABLE', 'Honda Civic with excellent fuel economy and reliable performance.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('HONDA-017', 5, 21, 1, 2021, 'HYBRID', 35000, 48.00, 'AVAILABLE', 'Honda Civic Hybrid with advanced fuel-saving technology.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('HONDA-018', 5, 22, 2, 2021, 'PETROL', 45000, 65.00, 'AVAILABLE', 'Honda Accord with spacious interior and smooth ride quality.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('HONDA-019', 5, 23, 3, 2021, 'PETROL', 35000, 75.00, 'AVAILABLE', 'Honda CR-V with excellent reliability and practical design.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('HONDA-020', 5, 24, 2, 2021, 'PETROL', 40000, 80.00, 'AVAILABLE', 'Honda Pilot with three-row seating and family-friendly features.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('HONDA-021', 5, 25, 1, 2021, 'PETROL', 30000, 35.00, 'AVAILABLE', 'Honda Fit with versatile interior and excellent fuel efficiency.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),

-- Additional Hyundai Vehicles (Brand ID: 6)
('HYUN-014', 6, 26, 1, 2021, 'PETROL', 45000, 38.00, 'AVAILABLE', 'Hyundai Elantra with modern design and excellent warranty coverage.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('HYUN-015', 6, 27, 2, 2021, 'PETROL', 50000, 58.00, 'AVAILABLE', 'Hyundai Sonata with comfortable ride and advanced safety features.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('HYUN-016', 6, 28, 3, 2021, 'PETROL', 40000, 68.00, 'AVAILABLE', 'Hyundai Tucson with modern styling and practical interior space.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('HYUN-017', 6, 29, 2, 2021, 'PETROL', 45000, 75.00, 'AVAILABLE', 'Hyundai Santa Fe with three-row seating and family comfort.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('HYUN-018', 6, 30, 3, 2021, 'PETROL', 35000, 60.00, 'AVAILABLE', 'Hyundai Kona with compact SUV practicality and modern features.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),

-- Additional Mercedes-Benz Vehicles (Brand ID: 7)
('MB-013', 7, 31, 7, 2021, 'PETROL', 25000, 190.00, 'AVAILABLE', 'Mercedes-Benz C-Class with luxury features and premium build quality.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('MB-014', 7, 32, 7, 2021, 'PETROL', 35000, 240.00, 'AVAILABLE', 'Mercedes-Benz E-Class with executive comfort and advanced technology.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('MB-015', 7, 33, 7, 2021, 'PETROL', 20000, 290.00, 'AVAILABLE', 'Mercedes-Benz S-Class with flagship luxury and cutting-edge features.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('MB-016', 7, 34, 3, 2021, 'PETROL', 30000, 210.00, 'AVAILABLE', 'Mercedes-Benz GLC with luxury SUV features and premium interior.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('MB-017', 7, 35, 2, 2021, 'PETROL', 15000, 340.00, 'AVAILABLE', 'Mercedes-Benz G-Class with legendary off-road capability and luxury.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),

-- Additional Porsche Vehicles (Brand ID: 8)
('POR-013', 8, 36, 8, 2021, 'PETROL', 12000, 380.00, 'AVAILABLE', 'Porsche 911 with timeless design and exceptional performance.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('POR-014', 8, 37, 3, 2021, 'PETROL', 25000, 340.00, 'AVAILABLE', 'Porsche Cayenne with sports car performance in SUV form.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('POR-015', 8, 38, 3, 2021, 'PETROL', 20000, 310.00, 'AVAILABLE', 'Porsche Macan with agile handling and premium features.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('POR-016', 8, 39, 7, 2021, 'PETROL', 18000, 440.00, 'AVAILABLE', 'Porsche Panamera with four-door sports car luxury and performance.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('POR-017', 8, 40, 11, 2021, 'ELECTRIC', 8000, 490.00, 'AVAILABLE', 'Porsche Taycan with all-electric performance and luxury features.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),

-- Additional Nissan Vehicles (Brand ID: 9)
('NISS-013', 9, 41, 2, 2021, 'PETROL', 40000, 52.00, 'AVAILABLE', 'Nissan Altima with comfortable ride and modern technology features.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('NISS-014', 9, 42, 1, 2021, 'PETROL', 45000, 42.00, 'AVAILABLE', 'Nissan Sentra with efficient performance and practical design.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('NISS-015', 9, 43, 3, 2021, 'PETROL', 35000, 65.00, 'AVAILABLE', 'Nissan Rogue with spacious interior and advanced safety technology.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('NISS-016', 9, 44, 11, 2021, 'ELECTRIC', 30000, 75.00, 'AVAILABLE', 'Nissan Leaf with all-electric performance and zero emissions.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('NISS-017', 9, 45, 8, 2021, 'PETROL', 15000, 190.00, 'AVAILABLE', 'Nissan GT-R with legendary performance and advanced technology.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),

-- Additional Kia Vehicles (Brand ID: 10)
('KIA-013', 10, 46, 3, 2021, 'PETROL', 40000, 55.00, 'AVAILABLE', 'Kia Sportage with modern design and excellent value proposition.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('KIA-014', 10, 47, 3, 2021, 'PETROL', 45000, 70.00, 'AVAILABLE', 'Kia Sorento with spacious interior and family-friendly features.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('KIA-015', 10, 48, 1, 2021, 'PETROL', 35000, 35.00, 'AVAILABLE', 'Kia Rio with excellent fuel efficiency and affordable maintenance.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('KIA-016', 10, 49, 2, 2021, 'PETROL', 20000, 80.00, 'AVAILABLE', 'Kia Stinger with sporty performance and premium features.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('KIA-017', 10, 50, 3, 2021, 'PETROL', 30000, 50.00, 'AVAILABLE', 'Kia Seltos with compact SUV practicality and modern styling.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),

-- Additional Audi Vehicles (Brand ID: 11)
('AUDI-014', 11, 51, 7, 2021, 'PETROL', 25000, 170.00, 'AVAILABLE', 'Audi A3 with premium build quality and advanced technology.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('AUDI-015', 11, 52, 7, 2021, 'PETROL', 35000, 210.00, 'AVAILABLE', 'Audi A4 with quattro all-wheel drive and luxury comfort.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('AUDI-016', 11, 53, 3, 2021, 'PETROL', 30000, 190.00, 'AVAILABLE', 'Audi Q5 with luxury SUV features and excellent performance.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('AUDI-017', 11, 54, 2, 2021, 'PETROL', 25000, 240.00, 'AVAILABLE', 'Audi Q7 with spacious three-row seating and premium features.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('AUDI-018', 11, 55, 8, 2021, 'PETROL', 8000, 390.00, 'AVAILABLE', 'Audi R8 with high-performance engine and exceptional handling.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),

-- Additional Tata Motors Vehicles (Brand ID: 12)
('TATA-012', 12, 56, 3, 2021, 'PETROL', 50000, 45.00, 'AVAILABLE', 'Tata Nexon with modern design and excellent value for money.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('TATA-013', 12, 57, 2, 2021, 'PETROL', 45000, 60.00, 'AVAILABLE', 'Tata Harrier with premium features and spacious interior.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('TATA-014', 12, 58, 1, 2021, 'PETROL', 55000, 30.00, 'AVAILABLE', 'Tata Tiago with economical performance and reliable build quality.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('TATA-015', 12, 59, 2, 2021, 'PETROL', 40000, 65.00, 'AVAILABLE', 'Tata Safari with family-friendly features and comfortable ride.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('TATA-016', 12, 60, 1, 2021, 'PETROL', 45000, 35.00, 'AVAILABLE', 'Tata Altroz with premium hatchback features and modern design.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),

-- Additional Tesla Vehicles (Brand ID: 13)
('TESLA-013', 13, 61, 11, 2021, 'ELECTRIC', 20000, 240.00, 'AVAILABLE', 'Tesla Model S with long-range electric performance and autopilot.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('TESLA-014', 13, 62, 11, 2021, 'ELECTRIC', 30000, 170.00, 'AVAILABLE', 'Tesla Model 3 with efficient electric performance and modern technology.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('TESLA-015', 13, 63, 11, 2021, 'ELECTRIC', 15000, 270.00, 'AVAILABLE', 'Tesla Model X with falcon-wing doors and advanced electric technology.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('TESLA-016', 13, 64, 11, 2021, 'ELECTRIC', 25000, 190.00, 'AVAILABLE', 'Tesla Model Y with compact SUV practicality and electric efficiency.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop');
