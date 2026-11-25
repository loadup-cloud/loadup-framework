CREATE TABLE IF NOT EXISTS kms_secret
(
    id         VARCHAR(64) NOT NULL PRIMARY KEY,
    ciphertext TEXT         NOT NULL CHECK (char_length(ciphertext) BETWEEN 1 AND 42000),
    created_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    deleted    BOOLEAN      NOT NULL DEFAULT FALSE
);
