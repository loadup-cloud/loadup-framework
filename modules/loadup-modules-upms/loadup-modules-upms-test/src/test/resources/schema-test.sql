-- Test Database Schema for UPMS Module (H2 Compatible)

-- Department Table
CREATE TABLE IF NOT EXISTS upms_department (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT DEFAULT 0,
    dept_name VARCHAR(100) NOT NULL,
    dept_code VARCHAR(50) NOT NULL UNIQUE,
    dept_level INT NOT NULL DEFAULT 1,
    sort_order INT NOT NULL DEFAULT 0,
    leader_user_id BIGINT,
    phone VARCHAR(20),
    email VARCHAR(100),
    status SMALLINT NOT NULL DEFAULT 1,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    remark VARCHAR(500),
    created_by BIGINT NOT NULL,
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_time TIMESTAMP
);

-- User Table
CREATE TABLE IF NOT EXISTS upms_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(200) NOT NULL,
    nickname VARCHAR(50),
    real_name VARCHAR(50),
    dept_id BIGINT NOT NULL,
    email VARCHAR(100) UNIQUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    phone VARCHAR(20) UNIQUE,
    phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
    avatar_url VARCHAR(500),
    gender SMALLINT DEFAULT 0,
    birthday DATE,
    status SMALLINT NOT NULL DEFAULT 1,
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_time TIMESTAMP,
    last_login_ip VARCHAR(50),
    login_fail_count INT NOT NULL DEFAULT 0,
    locked_time TIMESTAMP,
    password_update_time TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    remark VARCHAR(500),
    created_by BIGINT NOT NULL,
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_time TIMESTAMP
);

-- Role Table
CREATE TABLE IF NOT EXISTS upms_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(100) NOT NULL,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    parent_role_id BIGINT,
    role_level INT NOT NULL DEFAULT 1,
    data_scope SMALLINT NOT NULL DEFAULT 1,
    sort_order INT NOT NULL DEFAULT 0,
    status SMALLINT NOT NULL DEFAULT 1,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    remark VARCHAR(500),
    created_by BIGINT NOT NULL,
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_time TIMESTAMP
);

-- Permission Table
CREATE TABLE IF NOT EXISTS upms_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT DEFAULT 0,
    permission_name VARCHAR(100) NOT NULL,
    permission_code VARCHAR(100) NOT NULL UNIQUE,
    permission_type SMALLINT NOT NULL,
    resource_path VARCHAR(200),
    http_method VARCHAR(10),
    icon VARCHAR(100),
    component_path VARCHAR(200),
    sort_order INT NOT NULL DEFAULT 0,
    visible BOOLEAN NOT NULL DEFAULT TRUE,
    status SMALLINT NOT NULL DEFAULT 1,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    remark VARCHAR(500),
    created_by BIGINT NOT NULL,
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_time TIMESTAMP
);

-- User-Role Relation Table
CREATE TABLE IF NOT EXISTS upms_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Role-Permission Relation Table
CREATE TABLE IF NOT EXISTS upms_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Operation Log Table
CREATE TABLE IF NOT EXISTS upms_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    operation_module VARCHAR(50),
    operation_type VARCHAR(50),
    operation_desc VARCHAR(200),
    request_method VARCHAR(10),
    request_url VARCHAR(500),
    request_params TEXT,
    response_data TEXT,
    ip_address VARCHAR(50),
    location VARCHAR(100),
    browser VARCHAR(100),
    os VARCHAR(100),
    status SMALLINT NOT NULL,
    error_message TEXT,
    execution_time BIGINT,
    operation_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Login Log Table
CREATE TABLE IF NOT EXISTS upms_login_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    login_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    logout_time TIMESTAMP,
    ip_address VARCHAR(50),
    login_location VARCHAR(100),
    browser VARCHAR(100),
    os VARCHAR(100),
    login_status SMALLINT NOT NULL,
    login_message VARCHAR(200)
);

-- Insert test department
INSERT INTO upms_department (id, parent_id, dept_name, dept_code, dept_level, sort_order, status, deleted, created_by, created_time)
VALUES (1, 0, 'Test Department', 'TEST_DEPT', 1, 0, 1, FALSE, 1, CURRENT_TIMESTAMP);
