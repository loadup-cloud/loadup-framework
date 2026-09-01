---
-- #%L
-- Repository Database Plugin
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
-- Add authorize column to gateway_routes table.
-- Stores a route-level Spring Security SpEL expression or a comma-separated
-- authority/permission list evaluated after the security strategy.

ALTER TABLE gateway_routes
    ADD COLUMN authorize VARCHAR(512) NULL COMMENT 'Route-level authorization expression (SpEL or comma-separated authorities)'
        AFTER security_code;
