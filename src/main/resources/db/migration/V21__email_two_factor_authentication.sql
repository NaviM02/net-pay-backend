ALTER TABLE app_user ADD COLUMN two_factor_enabled BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE login_challenge (
    hash_id VARCHAR(50) PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_user(id),
    code_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    consumed BOOLEAN NOT NULL DEFAULT FALSE,
    fingerprint TEXT,
    user_agent TEXT,
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL
);
CREATE INDEX idx_login_challenge_user ON login_challenge(user_id);
