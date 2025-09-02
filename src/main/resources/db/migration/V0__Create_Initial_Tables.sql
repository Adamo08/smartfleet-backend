-- V1__init_schema.sql
-- Initial schema for vrspfab_api
-- Generated from mysqldump, cleaned for Flyway

CREATE TABLE addresses (
  id bigint NOT NULL AUTO_INCREMENT,
  city varchar(255) DEFAULT NULL,
  country varchar(255) DEFAULT NULL,
  state varchar(255) DEFAULT NULL,
  street varchar(255) DEFAULT NULL,
  zip_code varchar(255) DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE vehicle_brands (
  id bigint NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL,
  description varchar(255) DEFAULT NULL,
  is_active tinyint(1) DEFAULT '1',
  created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  country_of_origin varchar(255) DEFAULT NULL,
  logo_url varchar(255) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE vehicle_categories (
  id bigint NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL,
  description varchar(255) DEFAULT NULL,
  is_active tinyint(1) DEFAULT '1',
  created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  icon_url varchar(255) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE users (
  id bigint NOT NULL AUTO_INCREMENT,
  created_at datetime(6) DEFAULT NULL,
  email varchar(255) DEFAULT NULL,
  first_name varchar(255) DEFAULT NULL,
  last_name varchar(255) DEFAULT NULL,
  password varchar(255) DEFAULT NULL,
  phone_number varchar(255) DEFAULT NULL,
  role enum('ADMIN','CUSTOMER','GUEST') DEFAULT NULL,
  updated_at datetime(6) DEFAULT NULL,
  address_id bigint DEFAULT NULL,
  notification_preferences varchar(255) DEFAULT '{"emailEnabled": true, "realtimeEnabled": true}',
  auth_provider enum('LOCAL','FACEBOOK','GOOGLE') NOT NULL DEFAULT 'LOCAL',
  provider_id varchar(255) DEFAULT NULL,
  reset_token varchar(255) DEFAULT NULL,
  reset_token_expiry datetime(6) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY (email),
  UNIQUE KEY (phone_number),
  UNIQUE KEY (address_id),
  CONSTRAINT fk_users_address FOREIGN KEY (address_id) REFERENCES addresses (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE vehicle_models (
  id bigint NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL,
  brand_id bigint NOT NULL,
  description varchar(255) DEFAULT NULL,
  is_active tinyint(1) DEFAULT '1',
  created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_model_brand (name,brand_id),
  KEY brand_id (brand_id),
  CONSTRAINT fk_vehicle_models_brand FOREIGN KEY (brand_id) REFERENCES vehicle_brands (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE vehicles (
  id bigint NOT NULL AUTO_INCREMENT,
  fuel_type enum('CNG','DIESEL','ELECTRIC','HYBRID','LPG','N_A','OTHER','PETROL') DEFAULT NULL,
  license_plate varchar(255) DEFAULT NULL,
  mileage float DEFAULT NULL,
  status enum('AVAILABLE','DAMAGED','IN_MAINTENANCE','OTHER','OUT_OF_SERVICE','RENTED') NOT NULL,
  year int DEFAULT NULL,
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  description varchar(1000) DEFAULT NULL,
  image_url varchar(255) DEFAULT NULL,
  price_per_day double DEFAULT NULL,
  updated_at datetime(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  category_id bigint DEFAULT NULL,
  brand_id bigint DEFAULT NULL,
  model_id bigint DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY (license_plate),
  CONSTRAINT fk_vehicles_category FOREIGN KEY (category_id) REFERENCES vehicle_categories (id),
  CONSTRAINT fk_vehicles_brand FOREIGN KEY (brand_id) REFERENCES vehicle_brands (id),
  CONSTRAINT fk_vehicles_model FOREIGN KEY (model_id) REFERENCES vehicle_models (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE reservations (
  id bigint NOT NULL AUTO_INCREMENT,
  user_id bigint NOT NULL,
  vehicle_id bigint NOT NULL,
  start_date datetime(6) NOT NULL,
  end_date datetime(6) NOT NULL,
  status varchar(255) NOT NULL,
  comment varchar(500) DEFAULT NULL,
  booking_context json DEFAULT NULL,
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at datetime(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  KEY idx_reservation_user (user_id),
  KEY idx_reservation_vehicle (vehicle_id),
  KEY idx_reservation_start_date (start_date),
  KEY idx_reservation_status (status),
  CONSTRAINT fk_reservation_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_reservation_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE slots (
  id bigint NOT NULL AUTO_INCREMENT,
  vehicle_id bigint NOT NULL,
  start_time datetime(6) NOT NULL,
  end_time datetime(6) NOT NULL,
  is_available tinyint(1) NOT NULL DEFAULT 1,
  slot_type varchar(255) NOT NULL,
  price decimal(10,2) DEFAULT NULL,
  reservation_id bigint DEFAULT NULL,
  created_at datetime(6) DEFAULT CURRENT_TIMESTAMP(6),
  updated_at datetime(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  KEY fk_slots_vehicle (vehicle_id),
  KEY fk_slots_reservation (reservation_id),
  CONSTRAINT fk_slots_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles (id),
  CONSTRAINT fk_slots_reservation FOREIGN KEY (reservation_id) REFERENCES reservations (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE payments (
  id bigint NOT NULL AUTO_INCREMENT,
  reservation_id bigint NOT NULL,
  amount decimal(10,2) NOT NULL,
  refunded_amount decimal(10,2) DEFAULT 0.00,
  currency varchar(3) NOT NULL,
  status varchar(255) NOT NULL,
  transaction_id varchar(255) DEFAULT NULL,
  capture_id varchar(255) DEFAULT NULL,
  provider varchar(50) NOT NULL,
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY unique_payment_reservation (reservation_id),
  UNIQUE KEY unique_transaction_id (transaction_id),
  UNIQUE KEY unique_capture_id (capture_id),
  KEY idx_payment_reservation_id (reservation_id),
  KEY idx_payment_transaction_id (transaction_id),
  CONSTRAINT fk_payment_reservation FOREIGN KEY (reservation_id) REFERENCES reservations (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE refunds (
  id bigint NOT NULL AUTO_INCREMENT,
  payment_id bigint NOT NULL,
  refund_transaction_id varchar(255) DEFAULT NULL,
  amount decimal(10,2) NOT NULL,
  currency varchar(3) NOT NULL,
  reason varchar(255) NOT NULL,
  refund_method varchar(255) NOT NULL,
  additional_notes varchar(1000) DEFAULT NULL,
  contact_email varchar(255) DEFAULT NULL,
  contact_phone varchar(20) DEFAULT NULL,
  status varchar(255) NOT NULL,
  requested_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  processed_at datetime(6) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY fk_refund_payment (payment_id),
  CONSTRAINT fk_refund_payment FOREIGN KEY (payment_id) REFERENCES payments (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE activities (
  id bigint NOT NULL AUTO_INCREMENT,
  activity_type varchar(255) NOT NULL,
  title varchar(200) NOT NULL,
  description varchar(500) NOT NULL,
  user_id bigint DEFAULT NULL,
  related_entity_type varchar(50) DEFAULT NULL,
  related_entity_id bigint DEFAULT NULL,
  metadata json DEFAULT NULL,
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  KEY idx_activity_created_at (created_at),
  KEY idx_activity_type (activity_type),
  KEY idx_activity_user_id (user_id),
  CONSTRAINT fk_activity_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE bookmarks (
  id bigint NOT NULL AUTO_INCREMENT,
  user_id bigint NOT NULL,
  reservation_id bigint NOT NULL,
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at datetime(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_bookmark (user_id, reservation_id),
  KEY fk_bookmark_user (user_id),
  KEY fk_bookmark_reservation (reservation_id),
  CONSTRAINT fk_bookmark_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_bookmark_reservation FOREIGN KEY (reservation_id) REFERENCES reservations (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE favorites (
  id bigint NOT NULL AUTO_INCREMENT,
  user_id bigint NOT NULL,
  vehicle_id bigint NOT NULL,
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at datetime(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_favorite (user_id, vehicle_id),
  KEY fk_favorite_user (user_id),
  KEY fk_favorite_vehicle (vehicle_id),
  CONSTRAINT fk_favorite_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_favorite_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE testimonials (
  id bigint NOT NULL AUTO_INCREMENT,
  user_id bigint NOT NULL,
  vehicle_id bigint DEFAULT NULL,
  title varchar(255) DEFAULT NULL,
  content varchar(1000) NOT NULL,
  rating int NOT NULL,
  created_at datetime(6) DEFAULT CURRENT_TIMESTAMP(6),
  updated_at datetime(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  is_approved tinyint(1) NOT NULL DEFAULT 0,
  admin_reply_content varchar(1000) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY unique_user_vehicle_testimonial (user_id, vehicle_id),
  KEY fk_testimonial_user (user_id),
  KEY fk_testimonial_vehicle (vehicle_id),
  CONSTRAINT fk_testimonial_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_testimonial_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE notifications (
  id bigint NOT NULL AUTO_INCREMENT,
  user_id bigint NOT NULL,
  type varchar(255) NOT NULL,
  message varchar(500) NOT NULL,
  is_read tinyint(1) NOT NULL DEFAULT 0,
  created_at datetime(6) DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  KEY fk_notification_user (user_id),
  CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE email_templates (
  id bigint NOT NULL AUTO_INCREMENT,
  name varchar(100) NOT NULL,
  type varchar(255) NOT NULL,
  subject varchar(200) NOT NULL,
  description varchar(500) DEFAULT NULL,
  category varchar(50) NOT NULL,
  icon varchar(10) DEFAULT NULL,
  color varchar(20) DEFAULT NULL,
  template_file varchar(100) NOT NULL,
  is_active tinyint(1) NOT NULL DEFAULT 1,
  usage_count bigint DEFAULT 0,
  last_modified datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  created_by bigint DEFAULT NULL,
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY (name),
  KEY fk_email_template_created_by (created_by),
  CONSTRAINT fk_email_template_created_by FOREIGN KEY (created_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE email_template_variables (
  id bigint NOT NULL AUTO_INCREMENT,
  template_id bigint NOT NULL,
  name varchar(255) NOT NULL,
  description varchar(255) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY fk_variable_template (template_id),
  CONSTRAINT fk_variable_template FOREIGN KEY (template_id) REFERENCES email_templates (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE broadcast_history (
  id bigint NOT NULL AUTO_INCREMENT,
  title varchar(200) DEFAULT NULL,
  message text NOT NULL,
  type varchar(255) NOT NULL,
  target_type varchar(255) NOT NULL,
  target_value varchar(500) DEFAULT NULL,
  scheduled_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  sent_at datetime(6) DEFAULT NULL,
  status varchar(255) NOT NULL,
  sent_count bigint DEFAULT 0,
  delivered_count bigint DEFAULT 0,
  read_count bigint DEFAULT 0,
  click_count bigint DEFAULT 0,
  priority varchar(20) DEFAULT NULL,
  requires_confirmation tinyint(1) DEFAULT 0,
  track_analytics tinyint(1) DEFAULT 0,
  created_by bigint DEFAULT NULL,
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  KEY fk_broadcast_created_by (created_by),
  CONSTRAINT fk_broadcast_created_by FOREIGN KEY (created_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE user_notification_preferences (
  id bigint NOT NULL,
  real_time_enabled tinyint(1) NOT NULL DEFAULT 1,
  email_enabled tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (id),
  CONSTRAINT fk_pref_user FOREIGN KEY (id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
