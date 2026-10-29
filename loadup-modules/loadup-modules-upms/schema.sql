-- ============================================================================
-- LoadUp UPMS (User Permission Management System) Database Schema
-- RBAC3 Model: Role-Based Access Control with Role Hierarchy and Constraints
-- Database: PostgreSQL
-- Encoding: UTF-8
-- 规范：所有表必须包含 id/tenant_id/created_at/updated_at/deleted 标准字段
-- ============================================================================
-- ============================================================================
-- 1. Department (组织架构/部门表)
-- ============================================================================
CREATE TABLE IF NOT EXISTS upms_department
(
    id             VARCHAR(64)  NOT NULL,
    tenant_id      VARCHAR(64) DEFAULT '__default__',
    parent_id      VARCHAR(64)           DEFAULT '0',
    dept_name      VARCHAR(100) NOT NULL,
    dept_code      VARCHAR(50)  NOT NULL,
    dept_level     INT          NOT NULL DEFAULT 1,
    sort_order     INT          NOT NULL DEFAULT 0,
    leader_user_id VARCHAR(64),
    phone          VARCHAR(20),
    email          VARCHAR(100),
    status         SMALLINT      NOT NULL DEFAULT 1,
    remark         VARCHAR(500),
    created_by     VARCHAR(64),
    created_at     TIMESTAMP(6)     NOT NULL,
    updated_by     VARCHAR(64),
    updated_at     TIMESTAMP(6)     NOT NULL,
    deleted        BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE (dept_code)
);
CREATE INDEX IF NOT EXISTS idx_dept_parent_id ON upms_department (parent_id);
CREATE INDEX IF NOT EXISTS idx_dept_tenant_id ON upms_department (tenant_id);
CREATE INDEX IF NOT EXISTS idx_dept_status ON upms_department (status);

-- ============================================================================
-- 2. User (用户表)
-- ============================================================================
CREATE TABLE IF NOT EXISTS upms_user
(
    id                      VARCHAR(64)  NOT NULL,
    tenant_id               VARCHAR(64) DEFAULT '__default__',
    username                VARCHAR(50)  NOT NULL,
    password                VARCHAR(200) NOT NULL,
    nickname                VARCHAR(50),
    real_name               VARCHAR(50),
    dept_id                 VARCHAR(64)  NOT NULL,
    email                   VARCHAR(100),
    email_verified          BOOLEAN      NOT NULL DEFAULT FALSE,
    mobile                  VARCHAR(20),
    mobile_verified         BOOLEAN      NOT NULL DEFAULT FALSE,
    avatar                  VARCHAR(500),
    gender                  SMALLINT               DEFAULT 0,
    birthday                DATE,
    status                  SMALLINT      NOT NULL DEFAULT 1,
    account_non_expired     BOOLEAN      NOT NULL DEFAULT TRUE,
    account_non_locked      BOOLEAN      NOT NULL DEFAULT TRUE,
    credentials_non_expired BOOLEAN      NOT NULL DEFAULT TRUE,
    last_login_time         TIMESTAMP(6)     NULL,
    last_login_ip           VARCHAR(50),
    login_fail_count        INT          NOT NULL DEFAULT 0,
    locked_time             TIMESTAMP(6)     NULL,
    password_update_time    TIMESTAMP(6)     NULL,
    remark                  VARCHAR(500),
    created_by              VARCHAR(64),
    created_at              TIMESTAMP(6)     NOT NULL,
    updated_by              VARCHAR(64),
    updated_at              TIMESTAMP(6)     NOT NULL,
    deleted                 BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE (username),
    UNIQUE (email),
    UNIQUE (mobile)
);
CREATE INDEX IF NOT EXISTS idx_user_dept_id ON upms_user (dept_id);
CREATE INDEX IF NOT EXISTS idx_user_tenant_id ON upms_user (tenant_id);
CREATE INDEX IF NOT EXISTS idx_user_status ON upms_user (status);

-- ============================================================================
-- 3. Role (角色表)
-- ============================================================================
CREATE TABLE IF NOT EXISTS upms_role
(
    id             VARCHAR(64)  NOT NULL,
    tenant_id      VARCHAR(64) DEFAULT '__default__',
    role_name      VARCHAR(100) NOT NULL,
    role_code      VARCHAR(50)  NOT NULL,
    parent_role_id VARCHAR(64),
    role_level     INT          NOT NULL DEFAULT 1,
    data_scope     SMALLINT      NOT NULL DEFAULT 1,
    sort_order     INT          NOT NULL DEFAULT 0,
    status         SMALLINT      NOT NULL DEFAULT 1,
    remark         VARCHAR(500),
    created_by     VARCHAR(64),
    created_at     TIMESTAMP(6)     NOT NULL,
    updated_by     VARCHAR(64),
    updated_at     TIMESTAMP(6)     NOT NULL,
    deleted        BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE (role_code)
);
CREATE INDEX IF NOT EXISTS idx_role_parent_id ON upms_role (parent_role_id);
CREATE INDEX IF NOT EXISTS idx_role_tenant_id ON upms_role (tenant_id);
CREATE INDEX IF NOT EXISTS idx_role_status ON upms_role (status);

-- ============================================================================
-- 4. Permission (权限表)
-- ============================================================================
CREATE TABLE IF NOT EXISTS upms_permission
(
    id              VARCHAR(64)  NOT NULL,
    tenant_id       VARCHAR(64) DEFAULT '__default__',
    parent_id       VARCHAR(64)           DEFAULT '0',
    permission_name VARCHAR(100) NOT NULL,
    permission_code VARCHAR(100) NOT NULL,
    permission_type SMALLINT      NOT NULL,
    resource_path   VARCHAR(200),
    http_method     VARCHAR(10),
    icon            VARCHAR(100),
    component_path  VARCHAR(200),
    sort_order      INT          NOT NULL DEFAULT 0,
    visible         BOOLEAN      NOT NULL DEFAULT TRUE,
    status          SMALLINT      NOT NULL DEFAULT 1,
    remark          VARCHAR(500),
    created_by      VARCHAR(64),
    created_at      TIMESTAMP(6)     NOT NULL,
    updated_by      VARCHAR(64),
    updated_at      TIMESTAMP(6)     NOT NULL,
    deleted         BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE (permission_code)
);
CREATE INDEX IF NOT EXISTS idx_perm_parent_id ON upms_permission (parent_id);
CREATE INDEX IF NOT EXISTS idx_perm_tenant_id ON upms_permission (tenant_id);
CREATE INDEX IF NOT EXISTS idx_perm_type ON upms_permission (permission_type);
CREATE INDEX IF NOT EXISTS idx_perm_status ON upms_permission (status);

-- ============================================================================
-- 5. User-Role Relation (用户角色关联表)
-- ============================================================================
CREATE TABLE IF NOT EXISTS upms_user_role
(
    id         VARCHAR(64) NOT NULL,
    tenant_id  VARCHAR(64) DEFAULT '__default__',
    user_id    VARCHAR(64) NOT NULL,
    role_id    VARCHAR(64) NOT NULL,
    created_by VARCHAR(64),
    created_at TIMESTAMP(6)    NOT NULL,
    updated_at TIMESTAMP(6)    NOT NULL,
    deleted    BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE (user_id, role_id)
);
CREATE INDEX IF NOT EXISTS idx_ur_user_id ON upms_user_role (user_id);
CREATE INDEX IF NOT EXISTS idx_ur_role_id ON upms_user_role (role_id);
CREATE INDEX IF NOT EXISTS idx_ur_tenant_id ON upms_user_role (tenant_id);

-- ============================================================================
-- 6. Role-Permission Relation (角色权限关联表)
-- ============================================================================
CREATE TABLE IF NOT EXISTS upms_role_permission
(
    id            VARCHAR(64) NOT NULL,
    tenant_id     VARCHAR(64) DEFAULT '__default__',
    role_id       VARCHAR(64) NOT NULL,
    permission_id VARCHAR(64) NOT NULL,
    created_by    VARCHAR(64),
    created_at    TIMESTAMP(6)    NOT NULL,
    updated_at    TIMESTAMP(6)    NOT NULL,
    deleted       BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE (role_id, permission_id)
);
CREATE INDEX IF NOT EXISTS idx_rp_role_id ON upms_role_permission (role_id);
CREATE INDEX IF NOT EXISTS idx_rp_perm_id ON upms_role_permission (permission_id);
CREATE INDEX IF NOT EXISTS idx_rp_tenant_id ON upms_role_permission (tenant_id);

-- ============================================================================
-- 7. Role-Department Relation (角色部门关联表)
-- ============================================================================
CREATE TABLE IF NOT EXISTS upms_role_department
(
    id         VARCHAR(64) NOT NULL,
    tenant_id  VARCHAR(64) DEFAULT '__default__',
    role_id    VARCHAR(64) NOT NULL,
    dept_id    VARCHAR(64) NOT NULL,
    created_by VARCHAR(64),
    created_at TIMESTAMP(6)    NOT NULL,
    updated_at TIMESTAMP(6)    NOT NULL,
    deleted    BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE (role_id, dept_id)
);
CREATE INDEX IF NOT EXISTS idx_rd_role_id ON upms_role_department (role_id);
CREATE INDEX IF NOT EXISTS idx_rd_dept_id ON upms_role_department (dept_id);
CREATE INDEX IF NOT EXISTS idx_rd_tenant_id ON upms_role_department (tenant_id);

-- ============================================================================
-- 8. Operation Log — REMOVED
--    Operation logs are consolidated in loadup-modules-log (table: operation_log).
-- ============================================================================

-- ============================================================================
-- 9. Login Log (登录日志表)
-- ============================================================================
CREATE TABLE IF NOT EXISTS upms_login_log
(
    id             VARCHAR(64) NOT NULL,
    tenant_id      VARCHAR(64) DEFAULT '__default__',
    user_id        VARCHAR(64) NULL,
    username       VARCHAR(50) NOT NULL,
    login_time     TIMESTAMP(6)    NOT NULL,
    logout_time    TIMESTAMP(6)    NULL,
    ip_address     VARCHAR(50),
    login_location VARCHAR(100),
    browser        VARCHAR(50),
    os             VARCHAR(50),
    login_status   SMALLINT     NOT NULL DEFAULT 1,
    login_message  VARCHAR(500),
    login_type     VARCHAR(32),
    provider       VARCHAR(32),
    created_at     TIMESTAMP(6)    NOT NULL,
    updated_at     TIMESTAMP(6)    NOT NULL,
    deleted        BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS idx_login_user_id ON upms_login_log (user_id);
CREATE INDEX IF NOT EXISTS idx_login_tenant_id ON upms_login_log (tenant_id);
CREATE INDEX IF NOT EXISTS idx_login_time ON upms_login_log (login_time);
CREATE INDEX IF NOT EXISTS idx_login_status ON upms_login_log (login_status);

-- ============================================================================
-- 10. User Social Account (用户第三方社交账号表)
-- ============================================================================
CREATE TABLE IF NOT EXISTS upms_user_social
(
    id            VARCHAR(64)  NOT NULL,
    tenant_id     VARCHAR(64) DEFAULT '__default__',
    user_id       VARCHAR(64)  NOT NULL,
    provider      VARCHAR(50)  NOT NULL,
    open_id       VARCHAR(200) NOT NULL,
    union_id      VARCHAR(200),
    nickname      VARCHAR(100),
    avatar_url    VARCHAR(500),
    access_token  VARCHAR(500),
    refresh_token VARCHAR(500),
    expires_in    BIGINT,
    created_at    TIMESTAMP(6)     NOT NULL,
    updated_at    TIMESTAMP(6)     NOT NULL,
    deleted       BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE (provider, open_id)
);
CREATE INDEX IF NOT EXISTS idx_social_user_id ON upms_user_social (user_id);
CREATE INDEX IF NOT EXISTS idx_social_tenant_id ON upms_user_social (tenant_id);
CREATE INDEX IF NOT EXISTS idx_social_provider ON upms_user_social (provider);

-- ============================================================================
-- 11. OAuth Binding (用户OAuth绑定表)
-- ============================================================================
CREATE TABLE IF NOT EXISTS upms_user_oauth_binding
(
    id            VARCHAR(64)  NOT NULL,
    tenant_id     VARCHAR(64) DEFAULT '__default__',
    user_id       VARCHAR(64)  NOT NULL,
    provider      VARCHAR(32)  NOT NULL,
    open_id       VARCHAR(128) NOT NULL,
    union_id      VARCHAR(128),
    nickname      VARCHAR(64),
    avatar        VARCHAR(255),
    access_token  VARCHAR(512),
    refresh_token VARCHAR(512),
    expires_at    TIMESTAMP(6),
    bound_at      TIMESTAMP(6)     NOT NULL,
    created_at    TIMESTAMP(6)     NOT NULL,
    updated_at    TIMESTAMP(6)     NOT NULL,
    deleted       BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE (provider, open_id)
);
CREATE INDEX IF NOT EXISTS idx_oauth_user_id ON upms_user_oauth_binding (user_id);
CREATE INDEX IF NOT EXISTS idx_oauth_tenant_id ON upms_user_oauth_binding (tenant_id);
CREATE INDEX IF NOT EXISTS idx_oauth_provider ON upms_user_oauth_binding (provider);

-- ============================================================================
-- 12. Password Reset Token (密码重置令牌表)
-- ============================================================================
CREATE TABLE IF NOT EXISTS upms_password_reset_token
(
    id          VARCHAR(64)  NOT NULL,
    tenant_id   VARCHAR(64) DEFAULT '__default__',
    user_id     VARCHAR(64)  NOT NULL,
    token       VARCHAR(200) NOT NULL,
    expire_time TIMESTAMP(6)     NOT NULL,
    used        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP(6)     NOT NULL,
    updated_at  TIMESTAMP(6)     NOT NULL,
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE (token)
);
CREATE INDEX IF NOT EXISTS idx_reset_user_id ON upms_password_reset_token (user_id);
CREATE INDEX IF NOT EXISTS idx_reset_tenant_id ON upms_password_reset_token (tenant_id);
CREATE INDEX IF NOT EXISTS idx_reset_expire ON upms_password_reset_token (expire_time);

-- ============================================================================
-- 13. User Config (用户配置表)
-- ============================================================================
CREATE TABLE IF NOT EXISTS upms_user_config
(
    id                   VARCHAR(64) NOT NULL,
    tenant_id            VARCHAR(64) DEFAULT '__default__',
    user_id              VARCHAR(64) NOT NULL,
    theme                VARCHAR(50)          DEFAULT 'light',
    language             VARCHAR(20)          DEFAULT 'zh_CN',
    timezone             VARCHAR(50)          DEFAULT 'Asia/Shanghai',
    notification_enabled BOOLEAN              DEFAULT TRUE,
    config_json          TEXT,
    created_at           TIMESTAMP(6)    NOT NULL,
    updated_at           TIMESTAMP(6)    NOT NULL,
    deleted              BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE (user_id)
);
CREATE INDEX IF NOT EXISTS idx_config_tenant_id ON upms_user_config (tenant_id);

-- ============================================================================
-- Initialize Data
-- ============================================================================
INSERT INTO upms_department (id, parent_id, dept_name, dept_code, dept_level, sort_order, status, created_by,
                                    created_at,updated_at)
VALUES ('1', '0', '总公司', 'ROOT', 1, 0, 1, '1', NOW(),now()) ON CONFLICT DO NOTHING;

INSERT INTO upms_role (id, role_name, role_code, role_level, data_scope, sort_order, status, created_by,
                              created_at,updated_at)
VALUES ('1', '超级管理员', 'ROLE_SUPER_ADMIN', 1, 1, 0, 1, '1', NOW(),now()) ON CONFLICT DO NOTHING;

INSERT INTO upms_user (id, username, password, nickname, real_name, dept_id, email, mobile, status, created_by,
                              created_at,updated_at)
VALUES ('1', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iYS5TuIG',
        '超级管理员', 'Admin', '1', 'admin@loadup.com', '13800138000', 1, '1', NOW(),now()) ON CONFLICT DO NOTHING;

INSERT INTO upms_user_role (id, user_id, role_id, created_by, created_at,updated_at)
VALUES ('1', '1', '1', '1', NOW(),now()) ON CONFLICT DO NOTHING;

INSERT INTO upms_permission (id, parent_id, permission_name, permission_code, permission_type, resource_path,
                                    sort_order, status, created_by, created_at,updated_at)
VALUES ('1', '0', '系统管理', 'system', 1, '/system', 1, 1, '1', NOW(),now()),
       ('2', '1', '用户管理', 'system:user', 1, '/system/user', 1, 1, '1', NOW(),now()),
       ('3', '2', '用户查询', 'system:user:query', 2, NULL, 1, 1, '1', NOW(),now()),
       ('4', '2', '用户新增', 'system:user:create', 2, NULL, 2, 1, '1', NOW(),now()),
       ('5', '2', '用户修改', 'system:user:update', 2, NULL, 3, 1, '1', NOW(),now()),
       ('6', '2', '用户删除', 'system:user:delete', 2, NULL, 4, 1, '1', NOW(),now()),
       ('7', '1', '角色管理', 'system:role', 1, '/system/role', 2, 1, '1', NOW(),now()),
       ('8', '1', '部门管理', 'system:dept', 1, '/system/dept', 3, 1, '1', NOW(),now()),
       ('9', '1', '权限管理', 'system:permission', 1, '/system/perm', 4, 1, '1', NOW(),now()),
       ('10', '1', '审计中心', 'system:audit', 1, '/system/audit', 5, 1, '1', NOW(),now()),
       ('11', '1', '数据字典', 'system:dictionary', 1, '/system/dictionary', 6, 1, '1', NOW(),now()) ON CONFLICT DO NOTHING;

INSERT INTO upms_role_permission (id, role_id, permission_id, created_by, created_at,updated_at)
VALUES ('1', '1', '1', '1', NOW(),now()),
       ('2', '1', '2', '1', NOW(),now()),
       ('3', '1', '3', '1', NOW(),now()),
       ('4', '1', '4', '1', NOW(),now()),
       ('5', '1', '5', '1', NOW(),now()),
       ('6', '1', '6', '1', NOW(),now()),
       ('7', '1', '7', '1', NOW(),now()),
       ('8', '1', '8', '1', NOW(),now()),
       ('9', '1', '9', '1', NOW(),now()),
       ('10', '1', '10', '1', NOW(),now()),
       ('11', '1', '11', '1', NOW(),now()) ON CONFLICT DO NOTHING;

-- Contract API permissions for the local super administrator.
INSERT INTO upms_permission (id, parent_id, permission_name, permission_code, permission_type, resource_path,
                                    sort_order, status, created_by, created_at, updated_at)
VALUES ('contract-catalog-read', '0', '合约目录查询', 'contract:catalog:read', 3, NULL, 1, 1, '1', NOW(), NOW()),
       ('contract-catalog-write', '0', '合约目录编辑', 'contract:catalog:write', 3, NULL, 2, 1, '1', NOW(), NOW()),
       ('contract-catalog-publish', '0', '合约目录发布', 'contract:catalog:publish', 3, NULL, 3, 1, '1', NOW(), NOW()),
       ('contract-merchant-read', '0', '商户合约查询', 'contract:merchant:read', 3, NULL, 4, 1, '1', NOW(), NOW()),
       ('contract-merchant-sign', '0', '商户合约签约', 'contract:merchant:sign', 3, NULL, 5, 1, '1', NOW(), NOW()),
       ('contract-merchant-manage', '0', '商户合约管理', 'contract:merchant:manage', 3, NULL, 6, 1, '1', NOW(), NOW()),
       ('contract-runtime-resolve', '0', '合约运行解析', 'contract:runtime:resolve', 3, NULL, 7, 1, '1', NOW(), NOW()) ON CONFLICT DO NOTHING;

INSERT INTO upms_role_permission (id, role_id, permission_id, created_by, created_at, updated_at)
SELECT CONCAT('contract-rp-', REPLACE(permission_code, ':', '-')), '1', id, '1', NOW(), NOW()
FROM upms_permission
WHERE permission_code IN ('contract:catalog:read', 'contract:catalog:write', 'contract:catalog:publish',
                          'contract:merchant:read', 'contract:merchant:sign', 'contract:merchant:manage',
                          'contract:runtime:resolve') ON CONFLICT DO NOTHING;

-- Merchant permissions for the local integration launcher only; consumers manage their own grants.
INSERT INTO upms_permission (id, parent_id, permission_name, permission_code, permission_type, resource_path,
                                    sort_order, status, created_by, created_at, updated_at)
VALUES ('merchant-read', '0', '商户资料查询', 'merchant:read', 3, NULL, 1, 1, '1', NOW(), NOW()),
       ('merchant-write', '0', '商户资料编辑', 'merchant:write', 3, NULL, 2, 1, '1', NOW(), NOW()),
       ('merchant-manage', '0', '商户启停管理', 'merchant:manage', 3, NULL, 3, 1, '1', NOW(), NOW()) ON CONFLICT DO NOTHING;
INSERT INTO upms_role_permission (id, role_id, permission_id, created_by, created_at, updated_at)
SELECT CONCAT('merchant-rp-', REPLACE(permission_code, ':', '-')), '1', id, '1', NOW(), NOW()
FROM upms_permission WHERE permission_code IN ('merchant:read', 'merchant:write', 'merchant:manage') ON CONFLICT DO NOTHING;
