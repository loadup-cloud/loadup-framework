-- MySQL Database Schema for UPMS Testing
-- Drop tables if exist
DROP TABLE IF EXISTS upms_user_role;
DROP TABLE IF EXISTS upms_role_permission;
DROP TABLE IF EXISTS upms_role_department;
DROP TABLE IF EXISTS upms_operation_log;
DROP TABLE IF EXISTS upms_login_log;
DROP TABLE IF EXISTS upms_permission;
DROP TABLE IF EXISTS upms_role;
DROP TABLE IF EXISTS upms_user;
DROP TABLE IF EXISTS upms_department;

-- Department Table
CREATE TABLE upms_department
(
    id             VARCHAR(64)  NOT NULL PRIMARY KEY COMMENT '部门ID',
    parent_id      VARCHAR(64)           DEFAULT '0' COMMENT '父部门ID',
    dept_name      VARCHAR(100) NOT NULL COMMENT '部门名称',
    dept_code      VARCHAR(50)  NOT NULL UNIQUE COMMENT '部门编码',
    dept_level     INT          NOT NULL DEFAULT 1 COMMENT '部门层级',
    sort_order     INT          NOT NULL DEFAULT 0 COMMENT '排序',
    leader_user_id VARCHAR(64) COMMENT '负责人用户ID',
    phone          VARCHAR(20) COMMENT '联系电话',
    email          VARCHAR(100) COMMENT '邮箱',
    status         SMALLINT     NOT NULL DEFAULT 1 COMMENT '状态(1-启用 0-禁用)',
    deleted        TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标记(0-未删除 1-已删除)',
    remark         VARCHAR(500) COMMENT '备注',
    tenant_id      VARCHAR(64) COMMENT '租户ID',
    created_by     VARCHAR(64)  NOT NULL COMMENT '创建人',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by     VARCHAR(64) COMMENT '更新人',
    updated_at     DATETIME     NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_dept_parent_id (parent_id),
    INDEX idx_dept_code (dept_code),
    INDEX idx_dept_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='部门表';

-- User Table
CREATE TABLE upms_user
(
    id                      VARCHAR(64)  NOT NULL PRIMARY KEY COMMENT '用户ID',
    username                VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
    password                VARCHAR(200) NOT NULL COMMENT '密码',
    nickname                VARCHAR(50) COMMENT '昵称',
    real_name               VARCHAR(50) COMMENT '真实姓名',
    email                   VARCHAR(100) UNIQUE COMMENT '邮箱',
    email_verified          TINYINT               DEFAULT 0 COMMENT '邮箱是否验证',
    phone                   VARCHAR(20) UNIQUE COMMENT '手机号',
    phone_verified          TINYINT               DEFAULT 0 COMMENT '手机号是否验证',
    avatar                  VARCHAR(500) COMMENT '头像',
    gender                  SMALLINT              DEFAULT 0 COMMENT '性别(0-未知 1-男 2-女)',
    birthday                DATE COMMENT '生日',
    status                  SMALLINT     NOT NULL DEFAULT 1 COMMENT '状态(1-启用 0-禁用)',
    dept_id                 VARCHAR(64) COMMENT '部门ID',
    account_non_expired     TINYINT      NOT NULL DEFAULT 1 COMMENT '账号未过期',
    account_non_locked      TINYINT      NOT NULL DEFAULT 1 COMMENT '账号未锁定',
    credentials_non_expired TINYINT      NOT NULL DEFAULT 1 COMMENT '密码未过期',
    last_login_time         DATETIME COMMENT '最后登录时间',
    last_login_ip           VARCHAR(50) COMMENT '最后登录IP',
    login_fail_count        INT                   DEFAULT 0 COMMENT '登录失败次数',
    locked_time             DATETIME COMMENT '锁定时间',
    password_update_time    DATETIME COMMENT '密码更新时间',
    deleted                 TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标记',
    remark                  VARCHAR(500) COMMENT '备注',
    tenant_id               VARCHAR(64) COMMENT '租户ID',
    created_by              VARCHAR(64)  NOT NULL COMMENT '创建人',
    created_at              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by              VARCHAR(64) COMMENT '更新人',
    updated_at              DATETIME     NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_username (username),
    INDEX idx_user_email (email),
    INDEX idx_user_phone (phone),
    INDEX idx_user_dept_id (dept_id),
    INDEX idx_user_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户表';

-- Role Table
CREATE TABLE upms_role
(
    id         VARCHAR(64) NOT NULL PRIMARY KEY COMMENT '角色ID',
    parent_id  VARCHAR(64)          DEFAULT '0' COMMENT '父角色ID',
    role_name  VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code  VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    role_level INT         NOT NULL DEFAULT 1 COMMENT '角色层级',
    data_scope SMALLINT    NOT NULL DEFAULT 1 COMMENT '数据范围(1-全部 2-本部门及以下 3-本部门 4-仅本人 5-自定义)',
    sort_order INT         NOT NULL DEFAULT 0 COMMENT '排序',
    status     SMALLINT    NOT NULL DEFAULT 1 COMMENT '状态(1-启用 0-禁用)',
    deleted    TINYINT     NOT NULL DEFAULT 0 COMMENT '删除标记',
    remark     VARCHAR(500) COMMENT '备注',
    tenant_id  VARCHAR(64) COMMENT '租户ID',
    created_by VARCHAR(64) NOT NULL COMMENT '创建人',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(64) COMMENT '更新人',
    updated_at DATETIME    NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_role_code (role_code),
    INDEX idx_role_status (status),
    INDEX idx_role_parent_id (parent_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='角色表';

-- Permission Table
CREATE TABLE upms_permission
(
    id              VARCHAR(64)  NOT NULL PRIMARY KEY COMMENT '权限ID',
    parent_id       VARCHAR(64)           DEFAULT '0' COMMENT '父权限ID',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    permission_type SMALLINT     NOT NULL DEFAULT 1 COMMENT '权限类型(1-菜单 2-按钮 3-API)',
    resource_path   VARCHAR(200) COMMENT '资源路径',
    http_method     VARCHAR(10) COMMENT 'HTTP方法',
    sort_order      INT          NOT NULL DEFAULT 0 COMMENT '排序',
    icon            VARCHAR(100) COMMENT '图标',
    visible         TINYINT               DEFAULT 1 COMMENT '是否可见',
    status          SMALLINT     NOT NULL DEFAULT 1 COMMENT '状态(1-启用 0-禁用)',
    deleted         TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标记',
    remark          VARCHAR(500) COMMENT '备注',
    tenant_id       VARCHAR(64) COMMENT '租户ID',
    created_by      VARCHAR(64)  NOT NULL COMMENT '创建人',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by      VARCHAR(64) COMMENT '更新人',
    updated_at      DATETIME     NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_perm_parent_id (parent_id),
    INDEX idx_perm_code (permission_code),
    INDEX idx_perm_type (permission_type)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='权限表';

-- User-Role Relation Table
CREATE TABLE upms_user_role
(
    id         VARCHAR(64) NOT NULL PRIMARY KEY COMMENT 'ID',
    user_id    VARCHAR(64) NOT NULL COMMENT '用户ID',
    role_id    VARCHAR(64) NOT NULL COMMENT '角色ID',
    tenant_id  VARCHAR(64) COMMENT '租户ID',
    created_by VARCHAR(64) NOT NULL COMMENT '创建人',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_role_user_id (user_id),
    INDEX idx_user_role_role_id (role_id),
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户角色关联表';

-- Role-Permission Relation Table
CREATE TABLE upms_role_permission
(
    id            VARCHAR(64) NOT NULL PRIMARY KEY COMMENT 'ID',
    role_id       VARCHAR(64) NOT NULL COMMENT '角色ID',
    permission_id VARCHAR(64) NOT NULL COMMENT '权限ID',
    tenant_id     VARCHAR(64) COMMENT '租户ID',
    created_by    VARCHAR(64) NOT NULL COMMENT '创建人',
    created_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_role_perm_role_id (role_id),
    INDEX idx_role_perm_perm_id (permission_id),
    UNIQUE KEY uk_role_permission (role_id, permission_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='角色权限关联表';

-- Role-Department Relation Table
CREATE TABLE upms_role_department
(
    id            VARCHAR(64) NOT NULL PRIMARY KEY COMMENT 'ID',
    role_id       VARCHAR(64) NOT NULL COMMENT '角色ID',
    department_id VARCHAR(64) NOT NULL COMMENT '部门ID',
    tenant_id     VARCHAR(64) COMMENT '租户ID',
    created_by    VARCHAR(64) NOT NULL COMMENT '创建人',
    created_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_role_dept_role_id (role_id),
    INDEX idx_role_dept_dept_id (department_id),
    UNIQUE KEY uk_role_department (role_id, department_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='角色部门关联表';

-- Login Log Table
CREATE TABLE upms_login_log
(
    id             VARCHAR(64) NOT NULL PRIMARY KEY COMMENT '日志ID',
    user_id        VARCHAR(64) COMMENT '用户ID',
    username       VARCHAR(50) NOT NULL COMMENT '用户名',
    login_time     DATETIME    NOT NULL COMMENT '登录时间',
    logout_time    DATETIME    NOT NULL COMMENT '登录时间',
    ip_address     VARCHAR(50) COMMENT 'IP地址',
    login_location VARCHAR(100) COMMENT '登录地点',
    browser        VARCHAR(50) COMMENT '浏览器',
    os             VARCHAR(50) COMMENT '操作系统',
    login_status   SMALLINT    NOT NULL COMMENT '登录状态(1-成功 0-失败)',
    login_message  VARCHAR(500) COMMENT '登录消息',
    tenant_id      VARCHAR(64) COMMENT '租户ID',
    created_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted        TINYINT     NOT NULL DEFAULT 0 COMMENT '删除标记(0-未删除 1-已删除)',
    updated_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_login_log_user_id (user_id),
    INDEX idx_login_log_username (username),
    INDEX idx_login_log_time (login_time),
    INDEX idx_login_log_status (login_status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='登录日志表';

-- Operation Log Table
CREATE TABLE upms_operation_log
(
    id               VARCHAR(64) NOT NULL PRIMARY KEY COMMENT '日志ID',
    user_id          VARCHAR(64) COMMENT '用户ID',
    username         VARCHAR(50) COMMENT '用户名',
    operation_module VARCHAR(50) NOT NULL COMMENT '操作模块',
    operation_type   VARCHAR(20) NOT NULL COMMENT '操作类型',
    operation_desc   VARCHAR(200) COMMENT '操作描述',
    request_method   VARCHAR(10) COMMENT '请求方法',
    request_url      VARCHAR(500) COMMENT '请求URL',
    request_params   TEXT COMMENT '请求参数',
    response_result  TEXT COMMENT '响应结果',
    ip_address       VARCHAR(50) COMMENT 'IP地址',
    location         VARCHAR(100) COMMENT '操作地点',
    browser          VARCHAR(50) COMMENT '浏览器',
    os               VARCHAR(50) COMMENT '操作系统',
    operation_time   DATETIME    NOT NULL COMMENT '操作时间',
    execution_time   BIGINT COMMENT '执行时长(毫秒)',
    operation_status SMALLINT    NOT NULL COMMENT '操作状态(1-成功 0-失败)',
    error_message    TEXT COMMENT '错误消息',
    tenant_id        VARCHAR(64) COMMENT '租户ID',
    created_at       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_op_log_user_id (user_id),
    INDEX idx_op_log_username (username),
    INDEX idx_op_log_time (operation_time),
    INDEX idx_op_log_module (operation_module),
    INDEX idx_op_log_status (operation_status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='操作日志表';

