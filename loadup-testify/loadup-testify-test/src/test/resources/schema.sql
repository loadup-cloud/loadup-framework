---
-- #%L
-- Testify Demo
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
---
-- Database schema for Testify demo
drop table if exists users;
CREATE TABLE IF NOT EXISTS users
(
    user_id
               VARCHAR(100) PRIMARY KEY,
    user_name  VARCHAR(200) NOT NULL,
    email      VARCHAR(200) NULL,
    status     VARCHAR(50)  NULL DEFAULT 'ACTIVE',
    created_at datetime     NULL,
    updated_at datetime
);

