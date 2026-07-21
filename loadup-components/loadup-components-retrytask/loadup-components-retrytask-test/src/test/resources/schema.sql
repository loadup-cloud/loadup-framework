---
-- #%L
-- Loadup Components Retrytask Test
-- %%
-- Copyright (C) 2025 - 2026 loadup_cloud
-- %%
-- This program is free software: you can redistribute it and/or modify
-- it under the terms of the GNU General Public License as
-- published by the Free Software Foundation, either version 3 of the
-- License, or (at your option) any later version.
-- 
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU General Public License for more details.
-- 
-- You should have received a copy of the GNU General Public
-- License along with this program.  If not, see
-- <http://www.gnu.org/licenses/gpl-3.0.html>.
-- #L%
---
-- Retry Task Test Schema (与 schema-mysql.sql 保持一致)
DROP TABLE IF EXISTS `retry_task`;

CREATE TABLE IF NOT EXISTS `retry_task`
(
    `id`                  BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `biz_type`            VARCHAR(255) NOT NULL COMMENT '业务类型',
    `biz_id`              VARCHAR(255) NOT NULL COMMENT '业务ID',
    `retry_count`         INT(11)      NOT NULL DEFAULT '0' COMMENT '重试次数',
    `max_retry_count`     INT(11)      NOT NULL DEFAULT '0' COMMENT '最大重试次数',
    `next_retry_time`     DATETIME     NOT NULL COMMENT '下次重试时间',
    `status`              VARCHAR(255) NOT NULL COMMENT '状态',
    `priority`            INT(11)      NOT NULL DEFAULT '1' COMMENT '优先级权重(数值越大优先级越高: 10=HIGH, 1=LOW)',
    `last_failure_reason` TEXT COMMENT '失败原因',
    `created_at`          DATETIME     NOT NULL  COMMENT '创建时间',
    `updated_at`          DATETIME     NOT NULL  ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_biz` (`biz_type`, `biz_id`),
    KEY `idx_next_retry_time` (`next_retry_time`, `status`),
    KEY `idx_priority` (`priority`, `next_retry_time`, `status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='重试任务表';
