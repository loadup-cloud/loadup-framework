--
-- #%L
-- LoadUp Components Global Unique
-- %%
-- Copyright (C) 2025 - 2026 loadup_cloud
-- %%
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--      http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
-- #L%
--
CREATE TABLE IF NOT EXISTS global_unique
(
    id           VARCHAR(64)  NOT NULL COMMENT 'Primary key',
    tenant_id    VARCHAR(64)  NOT NULL COMMENT 'Tenant scope',
    unique_key   VARCHAR(255) NOT NULL COMMENT 'Business unique key',
    biz_type     VARCHAR(64)  NOT NULL COMMENT 'Business namespace',
    biz_id       VARCHAR(100)          DEFAULT NULL COMMENT 'Business identifier',
    request_data TEXT                  DEFAULT NULL COMMENT 'Request snapshot',
    created_at   DATETIME     NOT NULL COMMENT 'Creation time',
    updated_at   DATETIME     NOT NULL COMMENT 'Last update time',
    deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT 'Logical deletion flag',
    PRIMARY KEY (id),
    UNIQUE KEY uk_global_unique_scope (tenant_id, biz_type, unique_key),
    KEY idx_global_unique_biz (tenant_id, deleted, biz_type, biz_id),
    KEY idx_global_unique_created_at (created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='Tenant-scoped idempotency claims';
