DROP TABLE retry_task;
DROP TABLE retry_task_history;
CREATE TABLE `retry_task`
(
    `id`                   varchar(64) NOT NULL COMMENT '任务唯一标识',
    `business_type`        varchar(64) NOT NULL COMMENT '业务类型标识',
    `business_id`          varchar(64) NOT NULL COMMENT '业务ID',
    `retry_count`          int         NOT NULL                                         DEFAULT '0' COMMENT '已执行次数',
    `max_retries`          int         NOT NULL COMMENT '最大重试次数',
    `next_retry_time`      datetime    NOT NULL                                         DEFAULT '2025-01-01 00:00:00' COMMENT '下次执行时间',
    `reached_max_retries`  tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已达最大重试次数(1:是,0:否)',
    `processing`           tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否正在处理中(1:是,0:否)',
    `payload`              text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '任务上下文数据',
    `priority`             varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'L' COMMENT '任务优先级(H/L)',
    `created_time`         datetime    NOT NULL                                         DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`         datetime    NOT NULL                                         DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `trace_id`             varchar(64)                                                  DEFAULT NULL,
    `source`               varchar(64)                                                  DEFAULT NULL,
    `failure_callback_url` varchar(255)                                                 DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_business_identifier` (`business_id`,`business_type`),
    KEY                    `idx_next_retry` (`next_retry_time`,`business_type`),
    CONSTRAINT `chk_processing` CHECK ((`processing` in (_utf8mb4'1', _utf8mb4'0'))),
    CONSTRAINT `chk_reached_max` CHECK ((`reached_max_retries` in (_utf8mb4'1', _utf8mb4'0')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统重试任务表';

CREATE TABLE `retry_task_history`
(
    `id`                   varchar(64) NOT NULL COMMENT '任务唯一标识',
    `business_type`        varchar(64) NOT NULL COMMENT '业务类型标识',
    `business_id`          varchar(64) NOT NULL COMMENT '业务ID',
    `retry_count`          int         NOT NULL                                         DEFAULT '0' COMMENT '已执行次数',
    `max_retries`          int         NOT NULL COMMENT '最大重试次数',
    `next_retry_time`      datetime    NOT NULL                                         DEFAULT '2025-01-01 00:00:00' COMMENT '下次执行时间',
    `reached_max_retries`  tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已达最大重试次数(1:是,0:否)',
    `processing`           tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否正在处理中(1:是,0:否)',
    `payload`              text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '任务上下文数据',
    `priority`             varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'L' COMMENT '任务优先级(H/L)',
    `created_time`         datetime    NOT NULL                                         DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`         datetime    NOT NULL                                         DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `finished_time`        datetime    NOT NULL                                         DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `trace_id`             varchar(64)                                                  DEFAULT NULL,
    `source`               varchar(64)                                                  DEFAULT NULL,
    `failure_callback_url` varchar(255)                                                 DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_business_identifier` (`business_id`,`business_type`),
    KEY                    `idx_next_retry` (`next_retry_time`,`business_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统重试任务表';