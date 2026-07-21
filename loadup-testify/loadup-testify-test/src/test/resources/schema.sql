---
-- #%L
-- Testify Demo
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

