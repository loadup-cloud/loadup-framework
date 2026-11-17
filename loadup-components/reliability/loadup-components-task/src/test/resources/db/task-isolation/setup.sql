CREATE ROLE task_isolation_a LOGIN PASSWORD 'isolation-test-only' NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT;
CREATE ROLE task_isolation_b LOGIN PASSWORD 'isolation-test-only' NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT;
CREATE SCHEMA task_isolation_a;
CREATE SCHEMA task_isolation_b;
REVOKE ALL ON SCHEMA task_isolation_a, task_isolation_b FROM PUBLIC;
CREATE TABLE task_isolation_a.business_probe (
    id VARCHAR(64) PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE TABLE task_isolation_b.business_probe (LIKE task_isolation_a.business_probe INCLUDING ALL);
GRANT USAGE ON SCHEMA task_isolation_a TO task_isolation_a;
GRANT USAGE ON SCHEMA task_isolation_b TO task_isolation_b;
