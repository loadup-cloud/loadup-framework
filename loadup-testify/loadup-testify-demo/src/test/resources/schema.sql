-- Database schema for Testify demo
drop table if exists users;
CREATE TABLE IF NOT EXISTS users
(
    user_id
    VARCHAR
(
    100
) PRIMARY KEY,
    user_name VARCHAR
(
    200
) NOT NULL,
    email VARCHAR
(
    200
) NULL,
    status VARCHAR
(
    50
) NULL DEFAULT 'ACTIVE',
    created_at datetime NULL,
    updated_at datetime
    );

