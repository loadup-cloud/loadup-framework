-- MySQL Schema for Global Unique
-- 规范：所有表必须包含 id(VARCHAR64)/tenant_id/created_at/updated_at/deleted 标准字段

CREATE TABLE IF NOT EXISTS global_unique (
    id           VARCHAR(64)  NOT NULL                                               COMMENT 'ID',
    tenant_id    VARCHAR(64)                                                         COMMENT '租户ID',
    unique_key   VARCHAR(255) NOT NULL                                               COMMENT '唯一键(业务方自定义)',
    biz_type     VARCHAR(50)  NOT NULL                                               COMMENT '业务类型',
    biz_id       VARCHAR(100)          DEFAULT NULL                                  COMMENT '业务ID(可选)',
    request_data TEXT                  DEFAULT NULL                                  COMMENT '请求数据快照(可选)',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP                     COMMENT '创建时间',
    updated_at   DATETIME              NULL ON UPDATE CURRENT_TIMESTAMP              COMMENT '更新时间',
    deleted      TINYINT      NOT NULL DEFAULT 0                                     COMMENT '删除标记',
    PRIMARY KEY (id),
    UNIQUE KEY uk_unique_key (unique_key),
    KEY idx_tenant_id (tenant_id),
    KEY idx_biz_type  (biz_type),
    KEY idx_created_at(created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='全局唯一性控制表';
