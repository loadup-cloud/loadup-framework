-- ============================================================================
-- Flyway Migration Script
-- Version: 1
-- Description: Create initial schema with user and role tables
-- Author: LoadUp Framework
-- Date: 2026-02-06
-- ============================================================================

-- Create user table
CREATE TABLE IF NOT EXISTS user (
    id VARCHAR(32) PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(100) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(加密)',
    email VARCHAR(255) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE, INACTIVE, LOCKED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by VARCHAR(32) COMMENT '创建人',
    updated_by VARCHAR(32) COMMENT '更新人',
    UNIQUE KEY uk_username (username),
    KEY idx_email (email),
    KEY idx_phone (phone),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- Create role table
CREATE TABLE IF NOT EXISTS role (
    id VARCHAR(32) PRIMARY KEY COMMENT '角色ID',
    code VARCHAR(50) NOT NULL COMMENT '角色编码',
    name VARCHAR(100) NOT NULL COMMENT '角色名称',
    description VARCHAR(255) COMMENT '角色描述',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- Create user_role mapping table
CREATE TABLE IF NOT EXISTS user_role (
    id VARCHAR(32) PRIMARY KEY COMMENT 'ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    role_id VARCHAR(32) NOT NULL COMMENT '角色ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    created_by VARCHAR(32) COMMENT '创建人',
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_user_id (user_id),
    KEY idx_role_id (role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- Insert default admin user (password: admin123, should be changed in production)
INSERT INTO user (id, username, password, email, status, created_by)
VALUES ('admin', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin@loadup.io', 'ACTIVE', 'system');

-- Insert default roles
INSERT INTO role (id, code, name, description) VALUES
('role_admin', 'ADMIN', '管理员', '系统管理员角色'),
('role_user', 'USER', '普通用户', '普通用户角色');

-- Assign admin role to admin user
INSERT INTO user_role (id, user_id, role_id, created_by) VALUES
('ur_admin', 'admin', 'role_admin', 'system');
