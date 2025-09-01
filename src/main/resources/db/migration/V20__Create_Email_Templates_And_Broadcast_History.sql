-- Create email_templates table
CREATE TABLE email_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    subject VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    category VARCHAR(50) NOT NULL,
    icon VARCHAR(10),
    color VARCHAR(20),
    template_file VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    usage_count BIGINT DEFAULT 0,
    last_modified TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Create email_template_variables table for storing template variables
CREATE TABLE email_template_variables (
    template_id BIGINT NOT NULL,
    variable_name VARCHAR(100) NOT NULL,
    PRIMARY KEY (template_id, variable_name),
    FOREIGN KEY (template_id) REFERENCES email_templates(id) ON DELETE CASCADE
);

-- Create broadcast_history table
CREATE TABLE broadcast_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200),
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_value VARCHAR(500),
    scheduled_at TIMESTAMP NOT NULL,
    sent_at TIMESTAMP NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    sent_count BIGINT DEFAULT 0,
    delivered_count BIGINT DEFAULT 0,
    read_count BIGINT DEFAULT 0,
    click_count BIGINT DEFAULT 0,
    priority VARCHAR(20),
    requires_confirmation BOOLEAN DEFAULT FALSE,
    track_analytics BOOLEAN DEFAULT TRUE,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Create indexes for better performance
CREATE INDEX idx_email_templates_category ON email_templates(category);
CREATE INDEX idx_email_templates_type ON email_templates(type);
CREATE INDEX idx_email_templates_active ON email_templates(is_active);
CREATE INDEX idx_broadcast_history_status ON broadcast_history(status);
CREATE INDEX idx_broadcast_history_scheduled_at ON broadcast_history(scheduled_at);
CREATE INDEX idx_broadcast_history_created_by ON broadcast_history(created_by);

-- Insert some default email template metadata (referencing existing Thymeleaf templates)
INSERT INTO email_templates (name, type, subject, description, category, icon, color, template_file, is_active, created_at) VALUES
('Payment Success', 'PAYMENT_SUCCESS', 'Payment Confirmed - Reservation #{{reservationId}}', 'Sent when a payment is successfully processed', 'Payment', '💳', 'green', 'payment-success-email', true, CURRENT_TIMESTAMP),
('Payment Failure', 'PAYMENT_FAILURE', 'Payment Failed - Please Try Again', 'Sent when a payment transaction fails', 'Payment', '❌', 'red', 'payment-failure-email', true, CURRENT_TIMESTAMP),
('Reservation Confirmed', 'RESERVATION_CONFIRMED', 'Reservation Confirmed - {{vehicle}}', 'Sent when a reservation is successfully confirmed', 'Reservation', '✅', 'green', 'reservation-confirmed-email', true, CURRENT_TIMESTAMP),
('Welcome Email', 'ACCOUNT_VERIFICATION', 'Welcome to SmartFleet!', 'Sent to new users upon registration', 'Account', '👋', 'blue', 'welcome-email', true, CURRENT_TIMESTAMP);

-- Insert variables for the templates
INSERT INTO email_template_variables (template_id, variable_name) VALUES
(1, 'username'), (1, 'reservationId'), (1, 'vehicle'), (1, 'amount'), (1, 'currency'),
(2, 'username'), (2, 'reservationId'), (2, 'vehicle'), (2, 'amount'), (2, 'currency'), (2, 'paymentId'),
(3, 'username'), (3, 'reservationId'), (3, 'vehicle'), (3, 'start'), (3, 'end'),
(4, 'username'), (4, 'year');
