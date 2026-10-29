---
-- #%L
-- Loadup UPMS Test
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
-- Clear UPMS test tables before applying the production initial migration.
DROP TABLE IF EXISTS loadup_sequence_allocation;
DROP TABLE IF EXISTS upms_user_config;
DROP TABLE IF EXISTS upms_password_reset_token;
DROP TABLE IF EXISTS upms_user_oauth_binding;
DROP TABLE IF EXISTS upms_user_social;
DROP TABLE IF EXISTS upms_login_log;
DROP TABLE IF EXISTS upms_operation_log;
DROP TABLE IF EXISTS upms_role_department;
DROP TABLE IF EXISTS upms_role_permission;
DROP TABLE IF EXISTS upms_user_role;
DROP TABLE IF EXISTS upms_permission;
DROP TABLE IF EXISTS upms_user;
DROP TABLE IF EXISTS upms_role;
DROP TABLE IF EXISTS upms_department;
