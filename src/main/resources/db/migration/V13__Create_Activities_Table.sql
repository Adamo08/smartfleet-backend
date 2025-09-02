-- Migration script to create activities table for activity tracking
-- Author: SmartFleet System
-- Date: $(date)

CREATE TABLE IF NOT EXISTS activities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    activity_type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(500) NOT NULL,
    user_id BIGINT,
    related_entity_type VARCHAR(50),
    related_entity_id BIGINT,
    metadata JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    
    -- Indexes for performance
    INDEX idx_activity_created_at (created_at),
    INDEX idx_activity_type (activity_type),
    INDEX idx_activity_user_id (user_id),
    INDEX idx_activity_related_entity (related_entity_type, related_entity_id)
);

-- Add some sample data (only if not already present)
INSERT IGNORE INTO activities (activity_type, title, description, user_id, related_entity_type, related_entity_id, metadata) 
VALUES 
    ('SYSTEM_EVENT', 'System Initialized', 'Activity tracking system has been initialized and is ready to track activities', NULL, 'SYSTEM', NULL, '{"version": "1.0", "feature": "activity_tracking"}'),
    ('USER_REGISTRATION', 'Welcome Message', 'Activity tracking is now available for all user actions', NULL, 'SYSTEM', NULL, '{"message": "Track all important events"}');

-- Add comment to table
ALTER TABLE activities COMMENT = 'Stores activity logs for tracking user actions and system events';
