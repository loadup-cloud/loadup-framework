-- UPMS Test Schema (与 modules/loadup-modules-upms/schema.sql 保持一致)
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
    id             VARCHAR(64)  NOT NULL                                             COMMENT 'ID',
    tenant_id      VARCHAR(64)                                                       COMMENT '租户ID',
    parent_id      VARCHAR(64)           DEFAULT '0'                                COMMENT '父部门ID',
    dept_name      VARCHAR(100) NOT NULL                                             COMMENT '部门名称',
    dept_code      VARCHAR(50)  NOT NULL                                             COMMENT '部门编码',
    dept_level     INT          NOT NULL DEFAULT 1                                   COMMENT '部门层级',
    sort_order     INT          NOT NULL DEFAULT 0                                   COMMENT '排序序号',
    leader_user_id VARCHAR(64)                                                       COMMENT '部门负责人ID',
    phone          VARCHAR(20)                                                       COMMENT '联系电话',
    email          VARCHAR(100)                                                      COMMENT '联系邮箱',
    status         TINYINT      NOT NULL DEFAULT 1                                   COMMENT '状态：1-正常 0-停用',
    remark         VARCHAR(500)                                                      COMMENT '备注',
    created_by     VARCHAR(64)                                                       COMMENT '创建人ID',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP                   COMMENT '创建时间',
    updated_by     VARCHAR(64)                                                       COMMENT '更新人ID',
    updated_at     DATETIME              NULL ON UPDATE CURRENT_TIMESTAMP            COMMENT '更新时间',
    deleted        TINYINT      NOT NULL DEFAULT 0                                   COMMENT '删除标记',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dept_code (dept_code),
    INDEX idx_dept_parent_id (parent_id),
    INDEX idx_dept_tenant_id (tenant_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='部门表';

CREATE TABLE upms_user
(
    id                      VARCHAR(64)  NOT NULL                                    COMMENT 'ID',
    tenant_id               VARCHAR(64)                                              COMMENT '租户ID',
    username                VARCHAR(50)  NOT NULL                                    COMMENT '用户名',
    password                VARCHAR(200) NOT NULL                                    COMMENT '密码(BCrypt加密)',
    nickname                VARCHAR(50)                                              COMMENT '昵称',
    real_name               VARCHAR(50)                                              COMMENT '真实姓名',
    dept_id                 VARCHAR(64)  NOT NULL                                    COMMENT '所属部门ID',
    email                   VARCHAR(100)                                             COMMENT '邮箱',
    email_verified          TINYINT      NOT NULL DEFAULT 0                          COMMENT '邮箱是否验证',
    phone                   VARCHAR(20)                                              COMMENT '手机号',
    phone_verified          TINYINT      NOT NULL DEFAULT 0                          COMMENT '手机是否验证',
    avatar_url              VARCHAR(500)                                             COMMENT '头像地址',
    gender                  TINYINT               DEFAULT 0                          COMMENT '性别：0-未知 1-男 2-女',
    birthday                DATE                                                     COMMENT '生日',
    status                  TINYINT      NOT NULL DEFAULT 1                          COMMENT '状态：1-正常 0-停用 2-锁定',
    account_non_expired     TINYINT      NOT NULL DEFAULT 1                          COMMENT '账号是否未过期',
    account_non_locked      TINYINT      NOT NULL DEFAULT 1                          COMMENT '账号是否未锁定',
    credentials_non_expired TINYINT      NOT NULL DEFAULT 1                          COMMENT '密码是否未过期',
    last_login_time         DATETIME              NULL                               COMMENT '最后登录时间',
    last_login_ip           VARCHAR(50)                                              COMMENT '最后登录IP',
    login_fail_count        INT          NOT NULL DEFAULT 0                          COMMENT '登录失败次数',
    locked_time             DATETIME              NULL                               COMMENT '锁定时间',
    password_update_time    DATETIME              NULL                               COMMENT '密码更新时间',
    remark                  VARCHAR(500)                                             COMMENT '备注',
    created_by              VARCHAR(64)                                              COMMENT '创建人ID',
    created_at              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP          COMMENT '创建时间',
    updated_by              VARCHAR(64)                                              COMMENT '更新人ID',
    updated_at              DATETIME              NULL ON UPDATE CURRENT_TIMESTAMP   COMMENT '更新时间',
    deleted                 TINYINT      NOT NULL DEFAULT 0                          COMMENT '删除标记',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email),
    UNIQUE KEY uk_phone (phone),
    INDEX idx_user_dept_id (dept_id),
    INDEX idx_user_tenant_id (tenant_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='用户表';

CREATE TABLE upms_role
(
    id             VARCHAR(64)  NOT NULL                                             COMMENT 'ID',
    tenant_id      VARCHAR(64)                                                       COMMENT '租户ID',
    role_name      VARCHAR(100) NOT NULL                                             COMMENT '角色名称',
    role_code      VARCHAR(50)  NOT NULL                                             COMMENT '角色编码',
    parent_role_id VARCHAR(64)                                                       COMMENT '父角色ID',
    role_level     INT          NOT NULL DEFAULT 1                                   COMMENT '角色层级',
    data_scope     TINYINT      NOT NULL DEFAULT 1                                   COMMENT '数据权限',
    sort_order     INT          NOT NULL DEFAULT 0                                   COMMENT '排序序号',
    status         TINYINT      NOT NULL DEFAULT 1                                   COMMENT '状态',
    remark         VARCHAR(500)                                                      COMMENT '备注',
    created_by     VARCHAR(64)                                                       COMMENT '创建人ID',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP                   COMMENT '创建时间',
    updated_by     VARCHAR(64)                                                       COMMENT '更新人ID',
    updated_at     DATETIME              NULL ON UPDATE CURRENT_TIMESTAMP            COMMENT '更新时间',
    deleted        TINYINT      NOT NULL DEFAULT 0                                   COMMENT '删除标记',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code),
    INDEX idx_role_tenant_id (tenant_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='角色表';

CREATE TABLE upms_permission
(
    id              VARCHAR(64)  NOT NULL                                            COMMENT 'ID',
    tenant_id       VARCHAR(64)                                                      COMMENT '租户ID',
    parent_id       VARCHAR(64)           DEFAULT '0'                               COMMENT '父权限ID',
    permission_name VARCHAR(100) NOT NULL                                            COMMENT '权限名称',
    permission_code VARCHAR(100) NOT NULL                                            COMMENT '权限编码',
    permission_type TINYINT      NOT NULL                                            COMMENT '权限类型',
    resource_path   VARCHAR(200)                                                     COMMENT '资源路径',
    http_method     VARCHAR(10)                                                      COMMENT 'HTTP方法',
    icon            VARCHAR(100)                                                     COMMENT '图标',
    component_path  VARCHAR(200)                                                     COMMENT '前端组件路径',
    sort_order      INT          NOT NULL DEFAULT 0                                  COMMENT '排序序号',
    visible         TINYINT      NOT NULL DEFAULT 1                                  COMMENT '是否可见',
    status          TINYINT      NOT NULL DEFAULT 1                                  COMMENT '状态',
    remark          VARCHAR(500)                                                     COMMENT '备注',
    created_by      VARCHAR(64)                                                      COMMENT '创建人ID',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP                  COMMENT '创建时间',
    updated_by      VARCHAR(64)                                                      COMMENT '更新人ID',
    updated_at      DATETIME              NULL ON UPDATE CURRENT_TIMESTAMP           COMMENT '更新时间',
    deleted         TINYINT      NOT NULL DEFAULT 0                                  COMMENT '删除标记',
    PRIMARY KEY (id),
    UNIQUE KEY uk_perm_code (permission_code),
    INDEX idx_perm_tenant_id (tenant_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='权限表';

CREATE TABLE upms_user_role
(
    id         VARCHAR(64) NOT NULL                                                  COMMENT 'ID',
    tenant_id  VARCHAR(64)                                                           COMMENT '租户ID',
    user_id    VARCHAR(64) NOT NULL                                                  COMMENT '用户ID',
    role_id    VARCHAR(64) NOT NULL                                                  COMMENT '角色ID',
    created_by VARCHAR(64)                                                           COMMENT '创建人ID',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP                        COMMENT '创建时间',
    updated_at DATETIME             NULL ON UPDATE CURRENT_TIMESTAMP                COMMENT '更新时间',
    deleted    TINYINT     NOT NULL DEFAULT 0                                        COMMENT '删除标记',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_ur_user_id (user_id),
    INDEX idx_ur_role_id (role_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='用户角色关联表';

CREATE TABLE upms_role_permission
(
    id            VARCHAR(64) NOT NULL                                               COMMENT 'ID',
    tenant_id     VARCHAR(64)                                                        COMMENT '租户ID',
    role_id       VARCHAR(64) NOT NULL                                               COMMENT '角色ID',
    permission_id VARCHAR(64) NOT NULL                                               COMMENT '权限ID',
    created_by    VARCHAR(64)                                                        COMMENT '创建人ID',
    created_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP                     COMMENT '创建时间',
    updated_at    DATETIME             NULL ON UPDATE CURRENT_TIMESTAMP              COMMENT '更新时间',
    deleted       TINYINT     NOT NULL DEFAULT 0                                     COMMENT '删除标记',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_permission (role_id, permission_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='角色权限关联表';

CREATE TABLE upms_role_department
(
    id         VARCHAR(64) NOT NULL                                                  COMMENT 'ID',
    tenant_id  VARCHAR(64)                                                           COMMENT '租户ID',
    role_id    VARCHAR(64) NOT NULL                                                  COMMENT '角色ID',
    dept_id    VARCHAR(64) NOT NULL                                                  COMMENT '部门ID',
    created_by VARCHAR(64)                                                           COMMENT '创建人ID',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP                        COMMENT '创建时间',
    updated_at DATETIME             NULL ON UPDATE CURRENT_TIMESTAMP                COMMENT '更新时间',
    deleted    TINYINT     NOT NULL DEFAULT 0                                        COMMENT '删除标记',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_dept (role_id, dept_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='角色部门关联表';

CREATE TABLE upms_login_log
(
    id             VARCHAR(64)  NOT NULL                                             COMMENT 'ID',
    tenant_id      VARCHAR(64)                                                       COMMENT '租户ID',
    user_id        VARCHAR(64)  NOT NULL                                             COMMENT '用户ID',
    username       VARCHAR(50)  NOT NULL                                             COMMENT '用户名',
    login_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP                   COMMENT '登录时间',
    logout_time    DATETIME              NULL                                        COMMENT '登出时间',
    ip_address     VARCHAR(50)                                                       COMMENT '登录IP',
    login_location VARCHAR(100)                                                      COMMENT '登录地点',
    browser        VARCHAR(50)                                                       COMMENT '浏览器',
    os             VARCHAR(50)                                                       COMMENT '操作系统',
    login_status   TINYINT      NOT NULL DEFAULT 1                                   COMMENT '状态',
    login_message  VARCHAR(500)                                                      COMMENT '登录信息',
    login_type     VARCHAR(32)                                                       COMMENT '登录方式',
    provider       VARCHAR(32)                                                       COMMENT 'OAuth提供商',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP                   COMMENT '创建时间',
    updated_at     DATETIME              NULL ON UPDATE CURRENT_TIMESTAMP            COMMENT '更新时间',
    deleted        TINYINT      NOT NULL DEFAULT 0                                   COMMENT '删除标记',
    PRIMARY KEY (id),
    INDEX idx_login_user_id (user_id),
    INDEX idx_login_time (login_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='登录日志表';

CREATE TABLE upms_operation_log
(
    id               VARCHAR(64)  NOT NULL                                           COMMENT 'ID',
    tenant_id        VARCHAR(64)                                                     COMMENT '租户ID',
    trace_id         VARCHAR(64)                                                     COMMENT '链路追踪ID',
    user_id          VARCHAR(64)                                                     COMMENT '操作用户ID',
    username         VARCHAR(50)                                                     COMMENT '操作用户名',
    operation_type   VARCHAR(50)  NOT NULL                                           COMMENT '操作类型',
    operation_module VARCHAR(100) NOT NULL                                           COMMENT '操作模块',
    operation_desc   VARCHAR(500)                                                    COMMENT '操作描述',
    request_method   VARCHAR(10)                                                     COMMENT 'HTTP方法',
    request_url      VARCHAR(500)                                                    COMMENT '请求URL',
    request_params   TEXT                                                            COMMENT '请求参数',
    response_result  TEXT                                                            COMMENT '响应结果',
    ip_address       VARCHAR(50)                                                     COMMENT '操作IP',
    user_agent       VARCHAR(500)                                                    COMMENT '用户代理',
    execution_time   BIGINT                                                          COMMENT '执行耗时(ms)',
    status           TINYINT      NOT NULL DEFAULT 1                                 COMMENT '状态',
    error_message    TEXT                                                            COMMENT '错误信息',
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP                 COMMENT '创建时间',
    updated_at       DATETIME              NULL ON UPDATE CURRENT_TIMESTAMP          COMMENT '更新时间',
    deleted          TINYINT      NOT NULL DEFAULT 0                                 COMMENT '删除标记',
    PRIMARY KEY (id),
    INDEX idx_log_user_id (user_id),
    INDEX idx_log_tenant_id (tenant_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='操作日志表';

CREATE TABLE upms_user_oauth_binding
(
    id            VARCHAR(64)  NOT NULL                                              COMMENT 'ID',
    tenant_id     VARCHAR(64)                                                        COMMENT '租户ID',
    user_id       VARCHAR(64)  NOT NULL                                              COMMENT '本地用户ID',
    provider      VARCHAR(32)  NOT NULL                                              COMMENT 'OAuth提供商',
    open_id       VARCHAR(128) NOT NULL                                              COMMENT '第三方用户唯一ID',
    union_id      VARCHAR(128)                                                       COMMENT '联合ID',
    nickname      VARCHAR(64)                                                        COMMENT '第三方昵称',
    avatar        VARCHAR(255)                                                       COMMENT '第三方头像',
    access_token  VARCHAR(512)                                                       COMMENT '访问令牌',
    refresh_token VARCHAR(512)                                                       COMMENT '刷新令牌',
    expires_at    DATETIME                                                           COMMENT '令牌过期时间',
    bound_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP                   COMMENT '绑定时间',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP                   COMMENT '创建时间',
    updated_at    DATETIME              NULL ON UPDATE CURRENT_TIMESTAMP            COMMENT '更新时间',
    deleted       TINYINT      NOT NULL DEFAULT 0                                   COMMENT '删除标记',
    PRIMARY KEY (id),
    UNIQUE KEY uk_provider_openid (provider, open_id),
    INDEX idx_oauth_user_id (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='用户OAuth绑定表';
