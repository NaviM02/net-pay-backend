ALTER TABLE app_user
    ADD COLUMN reset_token_hash VARCHAR(255),
    ADD COLUMN reset_token_expires_at TIMESTAMP;
