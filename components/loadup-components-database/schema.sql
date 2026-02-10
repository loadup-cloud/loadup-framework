-- LoadUp Database Component - Sequence Table Schema
-- This script creates the sys_sequence table required by the sequence service

-- Drop table if exists (be careful in production)
-- DROP TABLE IF EXISTS sys_sequence;

-- Create sequence table
CREATE TABLE sys_sequence
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key',
    name       VARCHAR(100) NOT NULL UNIQUE COMMENT 'Sequence name, must be unique',
    value      BIGINT       NOT NULL DEFAULT 0 COMMENT 'Current sequence value',
    min_value  BIGINT       NOT NULL DEFAULT 0 COMMENT 'Minimum value for the sequence',
    max_value  BIGINT       NOT NULL DEFAULT 9223372036854775807 COMMENT 'Maximum value for the sequence',
    step       BIGINT       NOT NULL DEFAULT 1000 COMMENT 'Step size for range allocation',
    created_at TIMESTAMP             DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
    updated_at TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
COMMENT
='Sequence table for ID generation';

-- Create index on name for faster lookups
CREATE INDEX idx_sequence_name ON sys_sequence (name);

-- Insert some example sequences (optional)
-- INSERT INTO sys_sequence (name, value, min_value, max_value, step)
-- VALUES ('order_no', 0, 0, 9999999999, 1000),
--        ('user_id', 0, 0, 9999999999, 1000),
--        ('invoice_no', 0, 0, 9999999999, 1000);

