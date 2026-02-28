-- Log Module Test Schema (与 modules/loadup-modules-log/schema.sql 保持一致)
DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS operation_log;

CREATE TABLE IF NOT EXISTS operation_log (
    id             VARCHAR(64)  NOT NULL                                             COMMENT 'ID',
    tenant_id      VARCHAR(64)                                                       COMMENT '租户ID',
    user_id        VARCHAR(64)                                                       COMMENT '操作用户ID',
    username       VARCHAR(100)                                                      COMMENT '操作用户名',
    module         VARCHAR(50)  NOT NULL DEFAULT ''                                  COMMENT '模块',
    operation_type VARCHAR(20)  NOT NULL                                             COMMENT '操作类型',
    description    VARCHAR(500)                                                      COMMENT '操作描述',
    method         VARCHAR(500)                                                      COMMENT '方法名',
    request_params TEXT                                                              COMMENT '请求参数',
    response_result TEXT                                                             COMMENT '返回结果',
    duration       BIGINT                                                            COMMENT '执行时长(ms)',
    success        TINYINT      NOT NULL DEFAULT 1                                   COMMENT '是否成功',
    error_message  TEXT                                                              COMMENT '错误信息',
    ip             VARCHAR(128)                                                      COMMENT 'IP地址',
    user_agent     VARCHAR(500)                                                      COMMENT 'User-Agent',
    operation_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP                   COMMENT '操作时间',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP                   COMMENT '创建时间',
    updated_at     DATETIME              NULL ON UPDATE CURRENT_TIMESTAMP            COMMENT '更新时间',
    deleted        TINYINT      NOT NULL DEFAULT 0                                   COMMENT '删除标记',
    PRIMARY KEY (id),
    KEY idx_tenant_id      (tenant_id),
    KEY idx_user_id        (user_id),
    KEY idx_operation_type (operation_type),
    KEY idx_operation_time (operation_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

CREATE TABLE IF NOT EXISTS audit_log (
    id             VARCHAR(64)  NOT NULL                                             COMMENT 'ID',
    tenant_id      VARCHAR(64)                                                       COMMENT '租户ID',
    user_id        VARCHAR(64)  NOT NULL DEFAULT ''                                  COMMENT '操作用户ID',
    username       VARCHAR(100) NOT NULL DEFAULT ''                                  COMMENT '操作用户名',
    data_type      VARCHAR(50)  NOT NULL                                             COMMENT '数据类型',
    data_id        VARCHAR(64)                                                       COMMENT '数据ID',
    action         VARCHAR(20)  NOT NULL                                             COMMENT '操作',
    before_data    TEXT                                                              COMMENT '变更前数据',
    after_data     TEXT                                                              COMMENT '变更后数据',
    diff_data      TEXT                                                              COMMENT '差异数据',
    reason         VARCHAR(500)                                                      COMMENT '变更原因',
    ip             VARCHAR(128)                                                      COMMENT 'IP地址',
    operation_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP                   COMMENT '操作时间',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP                   COMMENT '创建时间',
    updated_at     DATETIME              NULL ON UPDATE CURRENT_TIMESTAMP            COMMENT '更新时间',
    deleted        TINYINT      NOT NULL DEFAULT 0                                   COMMENT '删除标记',
    PRIMARY KEY (id),
    KEY idx_tenant_id (tenant_id),
    KEY idx_user_id   (user_id),
    KEY idx_data_type (data_type),
    KEY idx_action    (action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';
