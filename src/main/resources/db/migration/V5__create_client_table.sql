-- CLIENT TABLE
CREATE TABLE client (
    id BIGSERIAL PRIMARY KEY,
    hash_id VARCHAR(50) NOT NULL UNIQUE,
    business_name VARCHAR(150) NOT NULL,
    contact_name VARCHAR(150) NOT NULL,
    contact_email VARCHAR(150),
    contact_phone VARCHAR(30),
    address VARCHAR(255),
    latitude NUMERIC(10,7),
    longitude NUMERIC(10,7),
    customer_since DATE,
    service_id BIGINT NOT NULL REFERENCES app_service(id),
    collector_id BIGINT REFERENCES app_user(id),
    tp_status BIGINT NOT NULL REFERENCES adm_typology(typology_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);
