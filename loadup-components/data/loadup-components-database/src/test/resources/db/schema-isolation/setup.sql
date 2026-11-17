CREATE ROLE schema_isolation_a LOGIN PASSWORD 'isolation-test-only' NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT;
CREATE ROLE schema_isolation_b LOGIN PASSWORD 'isolation-test-only' NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT;
CREATE SCHEMA schema_isolation_a;
CREATE SCHEMA schema_isolation_b;
REVOKE ALL ON SCHEMA schema_isolation_a, schema_isolation_b FROM PUBLIC;
CREATE TABLE schema_isolation_a.business_probe (
    id VARCHAR(64) PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    marker VARCHAR(64) NOT NULL
);
CREATE TABLE schema_isolation_b.business_probe (LIKE schema_isolation_a.business_probe INCLUDING ALL);
INSERT INTO schema_isolation_a.business_probe (id, marker) VALUES ('same-id', 'deployment-a');
INSERT INTO schema_isolation_b.business_probe (id, marker) VALUES ('same-id', 'deployment-b');
GRANT USAGE ON SCHEMA schema_isolation_a TO schema_isolation_a;
GRANT USAGE ON SCHEMA schema_isolation_b TO schema_isolation_b;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA schema_isolation_a TO schema_isolation_a;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA schema_isolation_b TO schema_isolation_b;
