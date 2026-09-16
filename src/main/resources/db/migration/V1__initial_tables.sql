-- 0. TYPOLOGIES
CREATE TABLE adm_typology (
    typology_id BIGINT PRIMARY KEY,
    internal_id BIGINT NOT NULL,
    parent_typology_id BIGINT,
    description VARCHAR(150) NOT NULL,
    value_1 VARCHAR(255),
    value_2 VARCHAR(255),
    CONSTRAINT uq_adm_typology_internal_id UNIQUE (internal_id),
    CONSTRAINT fk_adm_typology_parent FOREIGN KEY (parent_typology_id) REFERENCES adm_typology (typology_id)
);

-- 1. USER ACCOUNT TABLE
CREATE TABLE app_user (
    id BIGSERIAL PRIMARY KEY,
    hash_id VARCHAR(50) NOT NULL UNIQUE,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    tp_role BIGINT NOT NULL REFERENCES adm_typology(typology_id),
    tp_status BIGINT NOT NULL REFERENCES adm_typology(typology_id),
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- 2. SECURITY SESSION TABLE (TOKEN CREDENTIAL)
CREATE TABLE token_credential (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    token VARCHAR(750) NOT NULL UNIQUE,
    effective_date TIMESTAMP NOT NULL,
    expiration_date TIMESTAMP NOT NULL,
    user_agent VARCHAR(250),
    fingerprint VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_token_credential_dates CHECK (expiration_date > effective_date)
);

-- Index to optimize session checking on every single API request
CREATE INDEX idx_token_credential_user ON token_credential(user_id);
CREATE INDEX idx_token_credential_expiration ON token_credential(expiration_date);
