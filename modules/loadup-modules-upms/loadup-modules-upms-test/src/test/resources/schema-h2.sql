-- H2 Database Schema for UPMS Testing
-- Compatible with H2 in MySQL mode

-- Department Table
CREATE TABLE IF NOT EXISTS upms_department
(
    id             VARCHAR(64) PRIMARY KEY,
    parent_id      VARCHAR(64)           DEFAULT '0',
    dept_name      VARCHAR(100) NOT NULL,
    dept_code      VARCHAR(50)  NOT NULL UNIQUE,
    dept_level     INT          NOT NULL DEFAULT 1,
    sort_order     INT          NOT NULL DEFAULT 0,
    leader_user_id VARCHAR(64),
    phone          VARCHAR(20),
    email          VARCHAR(100),
    status         SMALLINT     NOT NULL DEFAULT 1,
    deleted        BOOLEAN      NOT NULL DEFAULT FALSE,
    remark         VARCHAR(500),
    tenant_id      VARCHAR(64),
    created_by     VARCHAR(64)  NOT NULL,
    create_time    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by     VARCHAR(64),
    update_time    TIMESTAMP    NULL
);

CREATE INDEX idx_dept_parent_id ON upms_department (parent_id);
CREATE INDEX idx_dept_code ON upms_department (dept_code);
CREATE INDEX idx_dept_status ON upms_department (status);

-- User Table
CREATE TABLE IF NOT EXISTS upms_user
(
    id          VARCHAR(64) PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(200) NOT NULL,
    nickname    VARCHAR(50),
    email       VARCHAR(100) UNIQUE,
    phone       VARCHAR(20) UNIQUE,
    avatar      VARCHAR(500),
    status      SMALLINT     NOT NULL DEFAULT 1,
    dept_id     VARCHAR(64),
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    remark      VARCHAR(500),
    tenant_id   VARCHAR(64),
    created_by  VARCHAR(64)  NOT NULL,
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by  VARCHAR(64),
    update_time TIMESTAMP    NULL
);

CREATE INDEX idx_user_username ON upms_user (username);
CREATE INDEX idx_user_email ON upms_user (email);
CREATE INDEX idx_user_phone ON upms_user (phone);
CREATE INDEX idx_user_dept_id ON upms_user (dept_id);
CREATE INDEX idx_user_status ON upms_user (status);

-- Role Table
CREATE TABLE IF NOT EXISTS upms_role
(
    id          VARCHAR(64) PRIMARY KEY,
    role_name   VARCHAR(50) NOT NULL,
    role_code   VARCHAR(50) NOT NULL UNIQUE,
    role_level  INT         NOT NULL DEFAULT 1,
    data_scope  SMALLINT    NOT NULL DEFAULT 1,
    status      SMALLINT    NOT NULL DEFAULT 1,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    remark      VARCHAR(500),
    tenant_id   VARCHAR(64),
    created_by  VARCHAR(64) NOT NULL,
    create_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by  VARCHAR(64),
    update_time TIMESTAMP   NULL
);

CREATE INDEX idx_role_code ON upms_role (role_code);
CREATE INDEX idx_role_status ON upms_role (status);

-- Permission Table
CREATE TABLE IF NOT EXISTS upms_permission
(
    id              VARCHAR(64) PRIMARY KEY,
    parent_id       VARCHAR(64)           DEFAULT '0',
    permission_name VARCHAR(100) NOT NULL,
    permission_code VARCHAR(100) NOT NULL UNIQUE,
    permission_type SMALLINT     NOT NULL DEFAULT 1,
    sort_order      INT          NOT NULL DEFAULT 0,
    icon            VARCHAR(100),
    router_path     VARCHAR(200),
    component_path  VARCHAR(200),
    status          SMALLINT     NOT NULL DEFAULT 1,
    deleted         BOOLEAN      NOT NULL DEFAULT FALSE,
    remark          VARCHAR(500),
    tenant_id       VARCHAR(64),
    created_by      VARCHAR(64)  NOT NULL,
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(64),
    update_time     TIMESTAMP    NULL
);

CREATE INDEX idx_perm_parent_id ON upms_permission (parent_id);
CREATE INDEX idx_perm_code ON upms_permission (permission_code);
CREATE INDEX idx_perm_type ON upms_permission (permission_type);
CREATE INDEX idx_perm_status ON upms_permission (status);

-- User-Role Relation Table
CREATE TABLE IF NOT EXISTS upms_user_role
(
    id          VARCHAR(64) PRIMARY KEY,
    user_id     VARCHAR(64) NOT NULL,
    role_id     VARCHAR(64) NOT NULL,
    tenant_id   VARCHAR(64),
    created_by  VARCHAR(64) NOT NULL,
    create_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_role_user_id ON upms_user_role (user_id);
CREATE INDEX idx_user_role_role_id ON upms_user_role (role_id);
CREATE UNIQUE INDEX uk_user_role ON upms_user_role (user_id, role_id);

-- Role-Permission Relation Table
CREATE TABLE IF NOT EXISTS upms_role_permission
(
    id            VARCHAR(64) PRIMARY KEY,
    role_id       VARCHAR(64) NOT NULL,
    permission_id VARCHAR(64) NOT NULL,
    tenant_id     VARCHAR(64),
    created_by    VARCHAR(64) NOT NULL,
    create_time   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_role_perm_role_id ON upms_role_permission (role_id);
CREATE INDEX idx_role_perm_perm_id ON upms_role_permission (permission_id);
CREATE UNIQUE INDEX uk_role_permission ON upms_role_permission (role_id, permission_id);

-- Login Log Table
CREATE TABLE IF NOT EXISTS upms_login_log
(
    id             VARCHAR(64) PRIMARY KEY,
    user_id        VARCHAR(64),
    username       VARCHAR(50) NOT NULL,
    login_time     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    login_ip       VARCHAR(50),
    login_location VARCHAR(100),
    browser        VARCHAR(100),
    os             VARCHAR(100),
    status         SMALLINT    NOT NULL DEFAULT 1,
    message        VARCHAR(500),
    tenant_id      VARCHAR(64),
    deleted        BOOLEAN     NOT NULL DEFAULT FALSE,
    created_by     VARCHAR(64) NOT NULL,
    create_time    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by     VARCHAR(64),
    update_time    TIMESTAMP   NULL
);

CREATE INDEX idx_login_log_user_id ON upms_login_log (user_id);
CREATE INDEX idx_login_log_username ON upms_login_log (username);
CREATE INDEX idx_login_log_time ON upms_login_log (login_time);
CREATE INDEX idx_login_log_status ON upms_login_log (status);

-- Operation Log Table
CREATE TABLE IF NOT EXISTS upms_operation_log
(
    id          VARCHAR(64) PRIMARY KEY,
    user_id     VARCHAR(64),
    username    VARCHAR(50) NOT NULL,
    operation   VARCHAR(100),
    method      VARCHAR(200),
    params      TEXT,
    time        BIGINT,
    ip          VARCHAR(50),
    tenant_id   VARCHAR(64),
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    created_by  VARCHAR(64) NOT NULL,
    create_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by  VARCHAR(64),
    update_time TIMESTAMP   NULL
);

CREATE INDEX idx_op_log_user_id ON upms_operation_log (user_id);
CREATE INDEX idx_op_log_username ON upms_operation_log (username);
CREATE INDEX idx_op_log_create_time ON upms_operation_log (create_time);

