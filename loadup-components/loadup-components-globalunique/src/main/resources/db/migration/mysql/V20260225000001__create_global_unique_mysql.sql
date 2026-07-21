---
-- #%L
-- LoadUp Components Global Unique
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
-- MySQL Schema for Global Unique
-- 规范：所有表必须包含 id(VARCHAR64)/tenant_id/created_at/updated_at/deleted 标准字段

CREATE TABLE IF NOT EXISTS global_unique
(
    id           VARCHAR(64)  NOT NULL COMMENT 'ID',
    tenant_id    VARCHAR(64) COMMENT '租户ID',
    unique_key   VARCHAR(255) NOT NULL COMMENT '唯一键(业务方自定义)',
    biz_type     VARCHAR(50)  NOT NULL COMMENT '业务类型',
    biz_id       VARCHAR(100)          DEFAULT NULL COMMENT '业务ID(可选)',
    request_data TEXT                  DEFAULT NULL COMMENT '请求数据快照(可选)',
    created_at   DATETIME     NOT NULL  COMMENT '创建时间',
    updated_at   DATETIME     NOT NULL COMMENT '更新时间',
    deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (id),
    UNIQUE KEY uk_unique_key (unique_key),
    KEY idx_tenant_id (tenant_id),
    KEY idx_biz_type (biz_type),
    KEY idx_created_at (created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='全局唯一性控制表';
