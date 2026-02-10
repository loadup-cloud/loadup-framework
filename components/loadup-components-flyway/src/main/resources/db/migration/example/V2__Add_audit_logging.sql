-- ============================================================================
-- Flyway Migration Script
-- Version: 2
-- Description: Add audit logging support
-- Author: LoadUp Framework
-- Date: 2026-02-06
-- ============================================================================

-- Create audit log table
CREATE TABLE IF NOT EXISTS audit_log (
    id VARCHAR(32) PRIMARY KEY COMMENT '日志ID',
    user_id VARCHAR(32) COMMENT '操作用户ID',
    username VARCHAR(100) COMMENT '操作用户名',
    action VARCHAR(50) NOT NULL COMMENT '操作类型: CREATE, UPDATE, DELETE, LOGIN, LOGOUT',
    resource_type VARCHAR(50) COMMENT '资源类型',
    resource_id VARCHAR(32) COMMENT '资源ID',
    details TEXT COMMENT '操作详情(JSON)',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    KEY idx_user_id (user_id),
    KEY idx_action (action),
    KEY idx_resource (resource_type, resource_id),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';

-- Add last_login_at to user table
ALTER TABLE user
ADD COLUMN last_login_at TIMESTAMP NULL COMMENT '最后登录时间',
ADD COLUMN last_login_ip VARCHAR(45) NULL COMMENT '最后登录IP',
ADD INDEX idx_last_login_at (last_login_at);
