---
-- #%L
-- LoadUp Components DFS Test
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
-- Test schema for DFS component with Spring Data JDBC

CREATE TABLE IF NOT EXISTS dfs_file_storage
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_id       VARCHAR(64)  NOT NULL UNIQUE,
    filename      VARCHAR(255) NOT NULL,
    content       BLOB         NOT NULL,
    size          BIGINT       NOT NULL,
    content_type  VARCHAR(100),
    hash          VARCHAR(64),
    biz_type      VARCHAR(50),
    biz_id        VARCHAR(64),
    metadata      VARCHAR(4096),
    public_access BOOLEAN   DEFAULT FALSE,
    upload_time   TIMESTAMP
);

CREATE INDEX idx_file_id ON dfs_file_storage (file_id);
CREATE INDEX idx_biz_type ON dfs_file_storage (biz_type);
CREATE INDEX idx_biz_id ON dfs_file_storage (biz_id);

