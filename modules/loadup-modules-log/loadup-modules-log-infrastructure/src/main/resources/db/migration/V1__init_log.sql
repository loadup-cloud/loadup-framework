-- ============================================================
-- V1: Init Log Module Tables
-- Module: loadup-modules-log
-- ============================================================

CREATE TABLE IF NOT EXISTS operation_log (
    id             VARCHAR(64)  NOT NULL                                COMMENT '主键',
    user_id        VARCHAR(64)                                          COMMENT '操作用户ID',
    username       VARCHAR(100)                                         COMMENT '操作用户名',
    module         VARCHAR(50)  NOT NULL DEFAULT ''                     COMMENT '模块',
    operation_type VARCHAR(20)  NOT NULL                                COMMENT '操作类型',
    description    VARCHAR(500)                                         COMMENT '操作描述',
    method         VARCHAR(500)                                         COMMENT '方法名',
    request_params TEXT                                                 COMMENT '请求参数',
    response_result TEXT                                                COMMENT '返回结果',
    duration       BIGINT                                               COMMENT '执行时长(ms)',
    success        BOOLEAN      NOT NULL DEFAULT TRUE                   COMMENT '是否成功',
    error_message  TEXT                                                 COMMENT '错误信息',
    ip             VARCHAR(128)                                         COMMENT 'IP地址',
    user_agent     VARCHAR(500)                                         COMMENT 'User-Agent',
    operation_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP      COMMENT '操作时间',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP      COMMENT '创建时间',
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                ON UPDATE CURRENT_TIMESTAMP             COMMENT '更新时间',
    deleted        BOOLEAN      NOT NULL DEFAULT FALSE                  COMMENT '是否删除',
    tenant_id      VARCHAR(64)                                          COMMENT '租户ID',
    PRIMARY KEY (id),
    KEY idx_user_id        (user_id),
    KEY idx_module         (module),
    KEY idx_operation_type (operation_type),
    KEY idx_operation_time (operation_time),
    KEY idx_success        (success)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

CREATE TABLE IF NOT EXISTS audit_log (
    id             VARCHAR(64)  NOT NULL                                COMMENT '主键',
    user_id        VARCHAR(64)  NOT NULL DEFAULT ''                     COMMENT '操作用户ID',
    username       VARCHAR(100) NOT NULL DEFAULT ''                     COMMENT '操作用户名',
    data_type      VARCHAR(50)  NOT NULL                                COMMENT '数据类型',
    data_id        VARCHAR(64)                                          COMMENT '数据ID',
    action         VARCHAR(20)  NOT NULL                                COMMENT '操作: CREATE/UPDATE/DELETE/ASSIGN',
    before_data    TEXT                                                 COMMENT '变更前数据(JSON)',
    after_data     TEXT                                                 COMMENT '变更后数据(JSON)',
    diff_data      TEXT                                                 COMMENT '差异数据(JSON)',
    reason         VARCHAR(500)                                         COMMENT '变更原因',
    ip             VARCHAR(128)                                         COMMENT 'IP地址',
    operation_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP      COMMENT '操作时间',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP      COMMENT '创建时间',
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                ON UPDATE CURRENT_TIMESTAMP             COMMENT '更新时间',
    deleted        BOOLEAN      NOT NULL DEFAULT FALSE                  COMMENT '是否删除',
    tenant_id      VARCHAR(64)                                          COMMENT '租户ID',
    PRIMARY KEY (id),
    KEY idx_user_id        (user_id),
    KEY idx_data_type      (data_type),
    KEY idx_data_id        (data_id),
    KEY idx_action         (action),
    KEY idx_operation_time (operation_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';

CREATE TABLE IF NOT EXISTS error_log (
    id             VARCHAR(64)  NOT NULL                                COMMENT '主键',
    user_id        VARCHAR(64)                                          COMMENT '用户ID',
    error_type     VARCHAR(50)  NOT NULL DEFAULT 'SYSTEM'               COMMENT '错误类型: BUSINESS/SYSTEM/THIRD_PARTY',
    error_code     VARCHAR(50)                                          COMMENT '错误码',
    error_message  TEXT         NOT NULL                                COMMENT '错误信息',
    stack_trace    TEXT                                                 COMMENT '堆栈信息',
    request_url    VARCHAR(500)                                         COMMENT '请求URL',
    request_method VARCHAR(10)                                          COMMENT '请求方法',
    request_params TEXT                                                 COMMENT '请求参数',
    ip             VARCHAR(128)                                         COMMENT 'IP地址',
    error_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP      COMMENT '错误时间',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP      COMMENT '创建时间',
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                ON UPDATE CURRENT_TIMESTAMP             COMMENT '更新时间',
    deleted        BOOLEAN      NOT NULL DEFAULT FALSE                  COMMENT '是否删除',
    tenant_id      VARCHAR(64)                                          COMMENT '租户ID',
    PRIMARY KEY (id),
    KEY idx_error_type  (error_type),
    KEY idx_error_code  (error_code),
    KEY idx_error_time  (error_time),
    KEY idx_user_id     (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='错误日志表';

    id             VARCHAR(64)  NOT NULL                                COMMENT '主键',
    user_id        VARCHAR(64)                                          COMMENT '操作用户ID',
    username       VARCHAR(100)                                         COMMENT '操作用户名',
    module         VARCHAR(50)  NOT NULL DEFAULT ''                     COMMENT '模块',
    operation_type VARCHAR(20)  NOT NULL                                COMMENT '操作类型',
    description    VARCHAR(500)                                         COMMENT '操作描述',
    method         VARCHAR(500)                                         COMMENT '方法名',
    request_params TEXT                                                 COMMENT '请求参数',
    response_result TEXT                                                COMMENT '返回结果',
    duration       BIGINT                                               COMMENT '执行时长(ms)',
    success        BOOLEAN      NOT NULL DEFAULT TRUE                   COMMENT '是否成功',
    error_message  TEXT                                                 COMMENT '错误信息',
    ip             VARCHAR(128)                                         COMMENT 'IP地址',
    user_agent     VARCHAR(500)                                         COMMENT 'User-Agent',
    operation_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP      COMMENT '操作时间',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP      COMMENT '创建时间',
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                ON UPDATE CURRENT_TIMESTAMP             COMMENT '更新时间',
    deleted        BOOLEAN      NOT NULL DEFAULT FALSE                  COMMENT '是否删除',
    tenant_id      VARCHAR(64)                                          COMMENT '租户ID',
    PRIMARY KEY (id),
    KEY idx_user_id        (user_id),
    KEY idx_module         (module),
    KEY idx_operation_type (operation_type),
    KEY idx_operation_time (operation_time),
    KEY idx_success        (success)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

CREATE TABLE IF NOT EXISTS audit_log (
    id             VARCHAR(64)  NOT NULL                                COMMENT '主键',
    user_id        VARCHAR(64)  NOT NULL DEFAULT ''                     COMMENT '操作用户ID',
    username       VARCHAR(100) NOT NULL DEFAULT ''                     COMMENT '操作用户名',
    data_type      VARCHAR(50)  NOT NULL                                COMMENT '数据类型',
    data_id        VARCHAR(64)                                          COMMENT '数据ID',
    action         VARCHAR(20)  NOT NULL                                COMMENT '操作: CREATE/UPDATE/DELETE/ASSIGN',
    before_data    TEXT                                                 COMMENT '变更前数据(JSON)',
    after_data     TEXT                                                 COMMENT '变更后数据(JSON)',
    diff_data      TEXT                                                 COMMENT '差异数据(JSON)',
    reason         VARCHAR(500)                                         COMMENT '变更原因',
    ip             VARCHAR(128)                                         COMMENT 'IP地址',
    operation_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP      COMMENT '操作时间',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP      COMMENT '创建时间',
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                ON UPDATE CURRENT_TIMESTAMP             COMMENT '更新时间',
    deleted        BOOLEAN      NOT NULL DEFAULT FALSE                  COMMENT '是否删除',
    tenant_id      VARCHAR(64)                                          COMMENT '租户ID',
    PRIMARY KEY (id),
    KEY idx_user_id        (user_id),
    KEY idx_data_type      (data_type),
    KEY idx_data_id        (data_id),
    KEY idx_action         (action),
    KEY idx_operation_time (operation_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';

