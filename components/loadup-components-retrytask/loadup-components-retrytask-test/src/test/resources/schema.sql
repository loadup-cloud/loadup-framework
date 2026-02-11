Drop TABLE IF EXISTS `retry_task`;

CREATE TABLE IF NOT EXISTS `retry_task`
(
    `id`                  bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `biz_type`            varchar(255) NOT NULL COMMENT '业务类型',
    `biz_id`              varchar(255) NOT NULL COMMENT '业务ID',
    `retry_count`         int(11)      NOT NULL DEFAULT '0' COMMENT '重试次数',
    `max_retry_count`     int(11)      NOT NULL DEFAULT '0' COMMENT '最大重试次数',
    `next_retry_time`     datetime     NOT NULL COMMENT '下次重试时间',
    `status`              varchar(255) NOT NULL COMMENT '状态',
    `priority`            int(11)      NOT NULL DEFAULT '1' COMMENT '优先级权重(数值越大优先级越高: 10=HIGH, 1=LOW)',
    `last_failure_reason` text COMMENT '失败原因',
    `create_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_biz` (`biz_type`, `biz_id`),
    KEY `idx_next_retry_time` (`next_retry_time`, `status`),
    KEY `idx_priority` (`priority`, `next_retry_time`, `status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='重试任务表';

