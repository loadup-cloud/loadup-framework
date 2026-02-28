-- Retry Task Test Schema (与 schema-mysql.sql 保持一致)
DROP TABLE IF EXISTS `retry_task`;

CREATE TABLE IF NOT EXISTS `retry_task`
(
    `id`                  VARCHAR(64)  NOT NULL                                      COMMENT 'ID',
    `tenant_id`           VARCHAR(64)                                                COMMENT '租户ID',
    `biz_type`            VARCHAR(255) NOT NULL                                      COMMENT '业务类型',
    `biz_id`              VARCHAR(255) NOT NULL                                      COMMENT '业务ID',
    `retry_count`         INT          NOT NULL DEFAULT 0                            COMMENT '重试次数',
    `max_retry_count`     INT          NOT NULL DEFAULT 0                            COMMENT '最大重试次数',
    `next_retry_time`     DATETIME     NOT NULL                                      COMMENT '下次重试时间',
    `status`              VARCHAR(255) NOT NULL                                      COMMENT '状态',
    `priority`            VARCHAR(10)  NOT NULL DEFAULT 'L'                          COMMENT '优先级',
    `last_failure_reason` TEXT                                                       COMMENT '失败原因',
    `created_at`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP            COMMENT '创建时间',
    `updated_at`          DATETIME              NULL ON UPDATE CURRENT_TIMESTAMP     COMMENT '更新时间',
    `deleted`             TINYINT      NOT NULL DEFAULT 0                            COMMENT '删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_biz` (`biz_type`, `biz_id`),
    KEY `idx_tenant_id`       (`tenant_id`),
    KEY `idx_next_retry_time` (`next_retry_time`, `status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='重试任务表';
