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
-- Add securityCode column to gateway_routes table
-- This column stores the authentication/authorization strategy code
-- Valid values: OFF, default, signature, internal, or custom codes

ALTER TABLE gateway_routes
    ADD COLUMN security_code VARCHAR(32) NULL COMMENT 'Security strategy code (OFF/default/signature/internal)'
        AFTER target;

-- Set default value for existing rows
UPDATE gateway_routes
SET security_code = 'default'
WHERE security_code IS NULL;

-- You can optionally make it NOT NULL after setting defaults
-- ALTER TABLE gateway_routes MODIFY COLUMN security_code VARCHAR(32) NOT NULL DEFAULT 'default';
