-- APP SERVICE TABLE
CREATE TABLE app_service (
    id BIGSERIAL PRIMARY KEY,
    hash_id VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    speed VARCHAR(50) NOT NULL,
    monthly_price NUMERIC(10,2) NOT NULL,
    tp_type BIGINT NOT NULL REFERENCES adm_typology(typology_id),
    tp_status BIGINT NOT NULL REFERENCES adm_typology(typology_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);