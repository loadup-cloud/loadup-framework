---
-- #%L
-- Loadup UPMS Test
-- %%
-- Copyright (C) 2025 - 2026 loadup_cloud
-- %%
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--      http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
-- #L%
---
-- UPMS Test Schema (与 modules/loadup-modules-upms/schema.sql 保持一致)
DROP TABLE IF EXISTS loadup_sequence_allocation;
DROP TABLE IF EXISTS upms_user_config;
DROP TABLE IF EXISTS upms_password_reset_token;
DROP TABLE IF EXISTS upms_user_oauth_binding;
DROP TABLE IF EXISTS upms_user_social;
DROP TABLE IF EXISTS upms_login_log;
DROP TABLE IF EXISTS upms_operation_log;
DROP TABLE IF EXISTS upms_role_department;
DROP TABLE IF EXISTS upms_role_permission;
DROP TABLE IF EXISTS upms_user_role;
DROP TABLE IF EXISTS upms_permission;
DROP TABLE IF EXISTS upms_user;
DROP TABLE IF EXISTS upms_role;
DROP TABLE IF EXISTS upms_department;

CREATE TABLE upms_department
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

CREATE TABLE upms_user
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

CREATE TABLE upms_role
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
CREATE INDEX IF NOT EXISTS idx_role_tenant_id ON upms_role (tenant_id);

CREATE TABLE upms_permission
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
CREATE INDEX IF NOT EXISTS idx_perm_tenant_id ON upms_permission (tenant_id);

CREATE TABLE upms_user_role
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

CREATE TABLE upms_role_permission
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


CREATE TABLE upms_role_department
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


CREATE TABLE upms_login_log
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
CREATE INDEX IF NOT EXISTS idx_login_time ON upms_login_log (login_time);

CREATE TABLE upms_operation_log
(
    id               VARCHAR(64)  NOT NULL,
    tenant_id        VARCHAR(64) DEFAULT '__default__',
    trace_id         VARCHAR(64),
    user_id          VARCHAR(64),
    username         VARCHAR(50),
    operation_type   VARCHAR(50)  NOT NULL,
    operation_module VARCHAR(100) NOT NULL,
    operation_desc   VARCHAR(500),
    request_method   VARCHAR(10),
    request_url      VARCHAR(500),
    request_params   TEXT,
    response_result  TEXT,
    ip_address       VARCHAR(50),
    user_agent       VARCHAR(500),
    execution_time   BIGINT,
    status           SMALLINT      NOT NULL DEFAULT 1,
    error_message    TEXT,
    created_at       TIMESTAMP(6)     NOT NULL,
    updated_at       TIMESTAMP(6)     NOT NULL,
    deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS idx_log_user_id ON upms_operation_log (user_id);
CREATE INDEX IF NOT EXISTS idx_log_tenant_id ON upms_operation_log (tenant_id);

CREATE TABLE upms_user_oauth_binding
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
