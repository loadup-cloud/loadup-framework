CREATE TABLE IF NOT EXISTS kms_key_file
(
    id         VARCHAR(64) PRIMARY KEY,
    content    BYTEA        NOT NULL CHECK (octet_length(content) BETWEEN 1 AND 4194304),
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    deleted    BOOLEAN      NOT NULL DEFAULT FALSE
);
