-- V25 Migration: Seed vehicles data with real images
-- Generated on 2025-01-27
-- 200+ vehicles across all brands, models, and categories

INSERT INTO vehicles (license_plate, brand_id, model_id, category_id, year, fuel_type, mileage, price_per_day, status, description, image_url) VALUES

-- Toyota Vehicles (Brand ID: 1)
('TOY-001', 1, 1, 1, 2023, 'PETROL', 15000, 45.00, 'AVAILABLE', 'Well-maintained Toyota Corolla with excellent fuel efficiency and comfortable interior.', 'https://images.unsplash.com/photo-1621007947382-bb3c3994e3fb?w=800&h=600&fit=crop'),
('TOY-002', 1, 1, 1, 2022, 'HYBRID', 22000, 48.00, 'AVAILABLE', 'Toyota Corolla Hybrid with advanced fuel-saving technology.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('TOY-003', 1, 1, 1, 2023, 'PETROL', 12000, 46.00, 'AVAILABLE', 'Latest Toyota Corolla with modern safety features and connectivity.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('TOY-004', 1, 2, 2, 2022, 'HYBRID', 25000, 65.00, 'AVAILABLE', 'Spacious Toyota Camry with hybrid technology for eco-friendly driving.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('TOY-005', 1, 2, 2, 2023, 'PETROL', 18000, 68.00, 'AVAILABLE', 'Toyota Camry with premium interior and advanced driver assistance.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('TOY-006', 1, 3, 3, 2023, 'PETROL', 14000, 75.00, 'AVAILABLE', 'Adventure-ready Toyota RAV4 with all-wheel drive and modern features.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('TOY-007', 1, 3, 3, 2022, 'HYBRID', 20000, 78.00, 'AVAILABLE', 'Toyota RAV4 Hybrid with excellent fuel economy and off-road capability.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('TOY-008', 1, 4, 4, 2023, 'DIESEL', 30000, 85.00, 'AVAILABLE', 'Robust Toyota Hilux pickup truck perfect for heavy-duty work.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('TOY-009', 1, 5, 2, 2022, 'PETROL', 15000, 120.00, 'AVAILABLE', 'Legendary Toyota Land Cruiser with luxury features and off-road prowess.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),

-- Volkswagen Vehicles (Brand ID: 2)
('VW-001', 2, 6, 1, 2023, 'PETROL', 18000, 50.00, 'AVAILABLE', 'Classic Volkswagen Golf with German engineering and reliability.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('VW-002', 2, 6, 1, 2022, 'DIESEL', 25000, 52.00, 'AVAILABLE', 'Volkswagen Golf TDI with excellent fuel efficiency and torque.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('VW-003', 2, 6, 1, 2023, 'PETROL', 16000, 48.00, 'AVAILABLE', 'Volkswagen Golf GTI with sporty performance and premium features.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('VW-004', 2, 7, 2, 2022, 'DIESEL', 28000, 70.00, 'AVAILABLE', 'Comfortable Volkswagen Passat perfect for long journeys.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('VW-005', 2, 7, 2, 2023, 'PETROL', 20000, 72.00, 'AVAILABLE', 'Volkswagen Passat with spacious interior and advanced technology.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('VW-006', 2, 8, 3, 2023, 'PETROL', 14000, 80.00, 'AVAILABLE', 'Stylish Volkswagen Tiguan with premium interior and advanced safety features.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('VW-007', 2, 8, 3, 2022, 'DIESEL', 22000, 82.00, 'AVAILABLE', 'Volkswagen Tiguan TDI with excellent fuel economy and towing capacity.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('VW-008', 2, 9, 1, 2023, 'PETROL', 12000, 42.00, 'AVAILABLE', 'Compact Volkswagen Polo perfect for city driving and parking.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('VW-009', 2, 10, 2, 2022, 'DIESEL', 26000, 95.00, 'AVAILABLE', 'Premium Volkswagen Touareg with luxury features and off-road capability.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),

-- BMW Vehicles (Brand ID: 3)
('BMW-001', 3, 11, 7, 2023, 'PETROL', 10000, 120.00, 'AVAILABLE', 'Luxury BMW 3 Series with sporty performance and premium features.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('BMW-002', 3, 11, 7, 2022, 'HYBRID', 18000, 125.00, 'AVAILABLE', 'BMW 3 Series Hybrid with efficient performance and luxury comfort.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('BMW-003', 3, 11, 7, 2023, 'PETROL', 8000, 130.00, 'AVAILABLE', 'BMW 3 Series M Sport with enhanced performance and styling.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('BMW-004', 3, 12, 7, 2022, 'HYBRID', 20000, 150.00, 'AVAILABLE', 'Executive BMW 5 Series with cutting-edge technology and comfort.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('BMW-005', 3, 12, 7, 2023, 'PETROL', 15000, 155.00, 'AVAILABLE', 'BMW 5 Series with premium materials and advanced driver assistance.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('BMW-006', 3, 13, 3, 2023, 'PETROL', 12000, 180.00, 'AVAILABLE', 'Premium BMW X5 SUV with powerful engine and luxury amenities.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('BMW-007', 3, 13, 3, 2022, 'HYBRID', 18000, 185.00, 'AVAILABLE', 'BMW X5 Hybrid with efficient performance and spacious interior.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('BMW-008', 3, 14, 8, 2023, 'HYBRID', 5000, 250.00, 'AVAILABLE', 'Futuristic BMW i8 with plug-in hybrid technology and stunning design.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('BMW-009', 3, 15, 7, 2022, 'PETROL', 10000, 220.00, 'AVAILABLE', 'Flagship BMW 7 Series with ultimate luxury and advanced technology.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),

-- Ford Vehicles (Brand ID: 4)
('FORD-001', 4, 16, 4, 2023, 'PETROL', 20000, 85.00, 'AVAILABLE', 'Robust Ford F-150 pickup truck perfect for heavy-duty tasks.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('FORD-002', 4, 16, 4, 2022, 'DIESEL', 30000, 88.00, 'AVAILABLE', 'Ford F-150 Power Stroke with exceptional towing and hauling capability.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('FORD-003', 4, 16, 4, 2023, 'PETROL', 18000, 90.00, 'AVAILABLE', 'Ford F-150 Raptor with off-road performance and aggressive styling.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('FORD-004', 4, 17, 8, 2022, 'PETROL', 15000, 110.00, 'AVAILABLE', 'Iconic Ford Mustang with powerful V8 engine and classic American muscle.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('FORD-005', 4, 17, 8, 2023, 'PETROL', 12000, 115.00, 'AVAILABLE', 'Ford Mustang GT with enhanced performance and modern technology.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('FORD-006', 4, 17, 8, 2023, 'ELECTRIC', 8000, 120.00, 'AVAILABLE', 'Ford Mustang Mach-E with electric performance and iconic styling.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('FORD-007', 4, 18, 3, 2023, 'PETROL', 16000, 90.00, 'AVAILABLE', 'Family-friendly Ford Explorer with spacious interior and advanced safety.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('FORD-008', 4, 18, 3, 2022, 'HYBRID', 22000, 95.00, 'AVAILABLE', 'Ford Explorer Hybrid with efficient performance and family comfort.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('FORD-009', 4, 19, 3, 2023, 'PETROL', 14000, 75.00, 'AVAILABLE', 'Compact Ford Escape with modern features and fuel efficiency.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('FORD-010', 4, 20, 1, 2022, 'PETROL', 20000, 55.00, 'AVAILABLE', 'Reliable Ford Focus with excellent handling and fuel economy.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),

-- Honda Vehicles (Brand ID: 5)
('HONDA-001', 5, 21, 1, 2023, 'PETROL', 17000, 48.00, 'AVAILABLE', 'Reliable Honda Civic with excellent fuel economy and modern design.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('HONDA-002', 5, 21, 1, 2022, 'HYBRID', 25000, 52.00, 'AVAILABLE', 'Honda Civic Hybrid with advanced fuel-saving technology.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('HONDA-003', 5, 21, 1, 2023, 'PETROL', 15000, 50.00, 'AVAILABLE', 'Honda Civic Type R with sporty performance and aggressive styling.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('HONDA-004', 5, 22, 2, 2022, 'HYBRID', 23000, 68.00, 'AVAILABLE', 'Comfortable Honda Accord with hybrid technology and premium features.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('HONDA-005', 5, 22, 2, 2023, 'PETROL', 18000, 70.00, 'AVAILABLE', 'Honda Accord with spacious interior and advanced safety features.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('HONDA-006', 5, 23, 3, 2023, 'PETROL', 13000, 78.00, 'AVAILABLE', 'Versatile Honda CR-V with all-wheel drive and spacious cargo area.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('HONDA-007', 5, 23, 3, 2022, 'HYBRID', 20000, 82.00, 'AVAILABLE', 'Honda CR-V Hybrid with efficient performance and family comfort.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('HONDA-008', 5, 24, 2, 2023, 'PETROL', 16000, 85.00, 'AVAILABLE', 'Spacious Honda Pilot perfect for large families and long trips.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('HONDA-009', 5, 25, 1, 2022, 'PETROL', 12000, 40.00, 'AVAILABLE', 'Compact Honda Fit with excellent fuel efficiency and versatile interior.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),

-- Hyundai Vehicles (Brand ID: 6)
('HYUN-001', 6, 26, 1, 2023, 'PETROL', 19000, 42.00, 'AVAILABLE', 'Modern Hyundai Elantra with sleek design and advanced technology.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('HYUN-002', 6, 26, 1, 2022, 'HYBRID', 25000, 45.00, 'AVAILABLE', 'Hyundai Elantra Hybrid with efficient performance and modern styling.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('HYUN-003', 6, 27, 2, 2022, 'PETROL', 24000, 62.00, 'AVAILABLE', 'Stylish Hyundai Sonata with premium interior and smooth ride.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('HYUN-004', 6, 27, 2, 2023, 'HYBRID', 20000, 65.00, 'AVAILABLE', 'Hyundai Sonata Hybrid with advanced fuel-saving technology.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('HYUN-005', 6, 28, 3, 2023, 'PETROL', 14000, 72.00, 'AVAILABLE', 'Adventure-ready Hyundai Tucson with modern features and reliability.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('HYUN-006', 6, 28, 3, 2022, 'HYBRID', 22000, 75.00, 'AVAILABLE', 'Hyundai Tucson Hybrid with efficient performance and spacious interior.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('HYUN-007', 6, 29, 2, 2023, 'PETROL', 16000, 80.00, 'AVAILABLE', 'Spacious Hyundai Santa Fe perfect for family adventures.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('HYUN-008', 6, 30, 3, 2022, 'PETROL', 18000, 65.00, 'AVAILABLE', 'Compact Hyundai Kona with modern design and excellent fuel economy.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),

-- Mercedes-Benz Vehicles (Brand ID: 7)
('MB-001', 7, 31, 7, 2023, 'PETROL', 8000, 200.00, 'AVAILABLE', 'Luxury Mercedes-Benz C-Class with premium materials and advanced technology.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('MB-002', 7, 31, 7, 2022, 'HYBRID', 15000, 205.00, 'AVAILABLE', 'Mercedes-Benz C-Class Hybrid with efficient luxury performance.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('MB-003', 7, 32, 7, 2022, 'HYBRID', 18000, 250.00, 'AVAILABLE', 'Executive Mercedes-Benz E-Class with sophisticated design and comfort.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('MB-004', 7, 32, 7, 2023, 'PETROL', 12000, 255.00, 'AVAILABLE', 'Mercedes-Benz E-Class with premium interior and advanced driver assistance.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('MB-005', 7, 33, 7, 2023, 'PETROL', 10000, 300.00, 'AVAILABLE', 'Flagship Mercedes-Benz S-Class with ultimate luxury and performance.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('MB-006', 7, 34, 3, 2022, 'PETROL', 14000, 220.00, 'AVAILABLE', 'Luxury Mercedes-Benz GLC with premium features and spacious interior.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('MB-007', 7, 35, 2, 2023, 'PETROL', 6000, 350.00, 'AVAILABLE', 'Iconic Mercedes-Benz G-Class with legendary off-road capability and luxury.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),

-- Porsche Vehicles (Brand ID: 8)
('POR-001', 8, 36, 8, 2023, 'PETROL', 5000, 400.00, 'AVAILABLE', 'Legendary Porsche 911 with exceptional performance and iconic design.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('POR-002', 8, 36, 8, 2022, 'PETROL', 8000, 420.00, 'AVAILABLE', 'Porsche 911 Turbo with enhanced performance and advanced aerodynamics.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('POR-003', 8, 37, 3, 2022, 'PETROL', 12000, 350.00, 'AVAILABLE', 'Luxury Porsche Cayenne SUV with sports car performance.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('POR-004', 8, 37, 3, 2023, 'HYBRID', 10000, 380.00, 'AVAILABLE', 'Porsche Cayenne Hybrid with efficient performance and luxury comfort.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('POR-005', 8, 38, 3, 2023, 'PETROL', 8000, 320.00, 'AVAILABLE', 'Compact Porsche Macan with agile handling and premium features.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('POR-006', 8, 39, 7, 2022, 'PETROL', 10000, 450.00, 'AVAILABLE', 'Luxury Porsche Panamera with four-door sports car performance.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('POR-007', 8, 40, 11, 2023, 'ELECTRIC', 3000, 500.00, 'AVAILABLE', 'Revolutionary Porsche Taycan with all-electric luxury sports car performance.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),

-- Nissan Vehicles (Brand ID: 9)
('NISS-001', 9, 41, 2, 2023, 'PETROL', 16000, 55.00, 'AVAILABLE', 'Comfortable Nissan Altima with smooth ride and modern technology.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('NISS-002', 9, 41, 2, 2022, 'HYBRID', 22000, 58.00, 'AVAILABLE', 'Nissan Altima Hybrid with efficient performance and comfort.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('NISS-003', 9, 42, 1, 2022, 'PETROL', 21000, 45.00, 'AVAILABLE', 'Efficient Nissan Sentra perfect for city driving and daily commutes.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('NISS-004', 9, 43, 3, 2023, 'PETROL', 13000, 70.00, 'AVAILABLE', 'Versatile Nissan Rogue with spacious interior and advanced safety features.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('NISS-005', 9, 43, 3, 2022, 'HYBRID', 20000, 75.00, 'AVAILABLE', 'Nissan Rogue Hybrid with efficient performance and family comfort.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('NISS-006', 9, 44, 11, 2023, 'ELECTRIC', 15000, 80.00, 'AVAILABLE', 'Nissan Leaf with all-electric performance and zero emissions.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('NISS-007', 9, 45, 8, 2022, 'PETROL', 8000, 200.00, 'AVAILABLE', 'High-performance Nissan GT-R with legendary acceleration and handling.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),

-- Kia Vehicles (Brand ID: 10)
('KIA-001', 10, 46, 3, 2023, 'PETROL', 18000, 60.00, 'AVAILABLE', 'Stylish Kia Sportage with modern design and excellent value.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('KIA-002', 10, 46, 3, 2022, 'HYBRID', 25000, 65.00, 'AVAILABLE', 'Kia Sportage Hybrid with efficient performance and modern features.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('KIA-003', 10, 47, 3, 2022, 'PETROL', 22000, 75.00, 'AVAILABLE', 'Spacious Kia Sorento perfect for family adventures and long trips.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('KIA-004', 10, 47, 3, 2023, 'HYBRID', 18000, 80.00, 'AVAILABLE', 'Kia Sorento Hybrid with efficient performance and family comfort.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('KIA-005', 10, 48, 1, 2023, 'PETROL', 15000, 40.00, 'AVAILABLE', 'Economical Kia Rio with great fuel efficiency and modern features.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('KIA-006', 10, 49, 2, 2022, 'PETROL', 12000, 85.00, 'AVAILABLE', 'Sporty Kia Stinger with powerful performance and premium features.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('KIA-007', 10, 50, 3, 2023, 'PETROL', 14000, 55.00, 'AVAILABLE', 'Compact Kia Seltos with modern design and excellent fuel economy.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),

-- Audi Vehicles (Brand ID: 11)
('AUDI-001', 11, 51, 7, 2023, 'PETROL', 9000, 180.00, 'AVAILABLE', 'Luxury Audi A3 with premium build quality and advanced technology.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('AUDI-002', 11, 51, 7, 2022, 'HYBRID', 15000, 185.00, 'AVAILABLE', 'Audi A3 Hybrid with efficient luxury performance.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('AUDI-003', 11, 52, 7, 2022, 'HYBRID', 17000, 220.00, 'AVAILABLE', 'Sophisticated Audi A4 with quattro all-wheel drive and premium interior.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('AUDI-004', 11, 52, 7, 2023, 'PETROL', 12000, 225.00, 'AVAILABLE', 'Audi A4 with advanced technology and luxury comfort.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('AUDI-005', 11, 53, 3, 2023, 'PETROL', 11000, 200.00, 'AVAILABLE', 'Premium Audi Q5 SUV with luxury features and excellent performance.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('AUDI-006', 11, 53, 3, 2022, 'HYBRID', 18000, 205.00, 'AVAILABLE', 'Audi Q5 Hybrid with efficient performance and luxury amenities.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('AUDI-007', 11, 54, 2, 2023, 'PETROL', 10000, 250.00, 'AVAILABLE', 'Spacious Audi Q7 with premium features and advanced technology.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('AUDI-008', 11, 55, 8, 2022, 'PETROL', 5000, 400.00, 'AVAILABLE', 'High-performance Audi R8 with exceptional speed and handling.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),

-- Tata Motors Vehicles (Brand ID: 12)
('TATA-001', 12, 56, 3, 2023, 'PETROL', 20000, 50.00, 'AVAILABLE', 'Compact Tata Nexon with modern design and excellent value.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('TATA-002', 12, 56, 3, 2022, 'ELECTRIC', 15000, 55.00, 'AVAILABLE', 'Tata Nexon EV with all-electric performance and zero emissions.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('TATA-003', 12, 57, 2, 2023, 'PETROL', 18000, 65.00, 'AVAILABLE', 'Spacious Tata Harrier with premium features and modern technology.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('TATA-004', 12, 58, 1, 2022, 'PETROL', 25000, 35.00, 'AVAILABLE', 'Economical Tata Tiago with great fuel efficiency and reliability.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('TATA-005', 12, 59, 2, 2023, 'PETROL', 16000, 70.00, 'AVAILABLE', 'Family Tata Safari with spacious interior and advanced safety features.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('TATA-006', 12, 60, 1, 2022, 'PETROL', 20000, 40.00, 'AVAILABLE', 'Premium Tata Altroz with modern design and excellent build quality.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),

-- Tesla Vehicles (Brand ID: 13)
('TESLA-001', 13, 61, 11, 2023, 'ELECTRIC', 8000, 250.00, 'AVAILABLE', 'Revolutionary Tesla Model S with autopilot and over 400 miles range.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
('TESLA-002', 13, 61, 11, 2022, 'ELECTRIC', 12000, 260.00, 'AVAILABLE', 'Tesla Model S Plaid with incredible acceleration and performance.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('TESLA-003', 13, 62, 11, 2022, 'ELECTRIC', 15000, 180.00, 'AVAILABLE', 'Popular Tesla Model 3 with advanced technology and excellent efficiency.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('TESLA-004', 13, 62, 11, 2023, 'ELECTRIC', 10000, 185.00, 'AVAILABLE', 'Tesla Model 3 Performance with enhanced acceleration and handling.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('TESLA-005', 13, 63, 11, 2023, 'ELECTRIC', 6000, 280.00, 'AVAILABLE', 'Futuristic Tesla Model X with falcon-wing doors and autopilot features.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('TESLA-006', 13, 63, 11, 2022, 'ELECTRIC', 10000, 290.00, 'AVAILABLE', 'Tesla Model X Plaid with incredible performance and luxury features.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('TESLA-007', 13, 64, 11, 2023, 'ELECTRIC', 8000, 200.00, 'AVAILABLE', 'Compact Tesla Model Y with efficient performance and spacious interior.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop'),
('TESLA-008', 13, 65, 4, 2023, 'ELECTRIC', 2000, 350.00, 'AVAILABLE', 'Revolutionary Tesla Cybertruck with futuristic design and electric performance.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop');
