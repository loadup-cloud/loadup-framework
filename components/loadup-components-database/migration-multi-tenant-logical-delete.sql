-- Migration script for adding multi-tenant and logical delete support
-- Execute this script to add required columns to existing tables

-- =============================================================================
-- STEP 1: Add tenant_id column to tables (Multi-Tenant Support)
-- =============================================================================

-- Example: Add tenant_id to user table
ALTER TABLE sys_user
    ADD COLUMN tenant_id VARCHAR(32) DEFAULT 'default' NOT NULL COMMENT '租户ID' AFTER id;

-- Create index on tenant_id for better query performance
CREATE INDEX idx_tenant_id ON sys_user (tenant_id);

-- Example: Add tenant_id to other business tables
-- ALTER TABLE your_table_name
-- ADD COLUMN tenant_id VARCHAR(32) DEFAULT 'default' NOT NULL COMMENT '租户ID';
-- CREATE INDEX idx_tenant_id ON your_table_name(tenant_id);

-- =============================================================================
-- STEP 2: Add deleted column to tables (Logical Delete Support)
-- =============================================================================

-- Example: Add deleted flag to user table
ALTER TABLE sys_user
    ADD COLUMN deleted BOOLEAN DEFAULT FALSE NOT NULL COMMENT '是否删除：false-未删除，true-已删除';

-- Create index on deleted for better query performance
CREATE INDEX idx_deleted ON sys_user (deleted);

-- Example: Add deleted flag to other business tables
-- ALTER TABLE your_table_name
-- ADD COLUMN deleted BOOLEAN DEFAULT FALSE NOT NULL COMMENT '是否删除';
-- CREATE INDEX idx_deleted ON your_table_name(deleted);

-- =============================================================================
-- STEP 3: Add timestamp columns (Auto-fill Support)
-- =============================================================================

-- Example: Add created_at and updated_at to user table
ALTER TABLE sys_user
    ADD COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP                             NOT NULL COMMENT '创建时间',
    ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间';

-- Example: Add timestamp columns to other business tables
-- ALTER TABLE your_table_name
-- ADD COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
-- ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间';

-- =============================================================================
-- STEP 4: Create composite indexes for better performance
-- =============================================================================

-- Create composite index for multi-tenant queries
-- This index improves performance for queries filtered by tenant_id and deleted
CREATE INDEX idx_tenant_deleted ON sys_user (tenant_id, deleted);

-- For tables with frequent queries, consider adding more columns to the composite index
-- Example: CREATE INDEX idx_tenant_deleted_status ON sys_user(tenant_id, deleted, status);

-- =============================================================================
-- STEP 5: Update existing data (if needed)
-- =============================================================================

-- If you have existing data without tenant_id, you need to set a default value
-- UPDATE sys_user SET tenant_id = 'default' WHERE tenant_id IS NULL;

-- If you have existing data without deleted flag, initialize it to false
-- UPDATE sys_user SET deleted = FALSE WHERE deleted IS NULL;

-- =============================================================================
-- STEP 6: Create tenant table (Optional)
-- =============================================================================

-- Create tenant table if you want to manage tenant configurations
CREATE TABLE IF NOT EXISTS sys_tenant
(
    id                     VARCHAR(32) PRIMARY KEY COMMENT '租户ID',
    name                   VARCHAR(100)                                                   NOT NULL COMMENT '租户名称',
    code                   VARCHAR(50)                                                    NOT NULL UNIQUE COMMENT '租户编码',
    status                 INT      DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    logical_delete_enabled BOOLEAN  DEFAULT FALSE COMMENT '是否启用逻辑删除',
    description            VARCHAR(500) COMMENT '租户描述',
    contact_name           VARCHAR(50) COMMENT '联系人姓名',
    contact_email          VARCHAR(100) COMMENT '联系人邮箱',
    contact_phone          VARCHAR(20) COMMENT '联系人电话',
    created_at             DATETIME DEFAULT CURRENT_TIMESTAMP                             NOT NULL COMMENT '创建时间',
    updated_at             DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间',
    INDEX idx_code (code),
    INDEX idx_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='租户表';

-- Insert default tenant
INSERT INTO sys_tenant (id, name, code, status, logical_delete_enabled, description)
VALUES ('default', '默认租户', 'default', 1, TRUE, '系统默认租户')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- =============================================================================
-- NOTES:
-- =============================================================================
-- 1. Replace 'sys_user' with your actual table names
-- 2. Adjust column types and defaults according to your requirements
-- 3. For existing tables with data, consider:
--    - Running the migration in a transaction
--    - Testing on a backup database first
--    - Checking data integrity after migration
-- 4. Tables in ignore-tables list (like sys_tenant, sys_config) don't need tenant_id
-- 5. Consider the performance impact of adding indexes on large tables
-- 6. For MySQL 5.7 and earlier, BOOLEAN is stored as TINYINT(1)

