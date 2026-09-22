-- PAYMENT TABLE
CREATE TABLE payment (
    id BIGSERIAL PRIMARY KEY,
    hash_id VARCHAR(50) NOT NULL UNIQUE,
    client_id BIGINT NOT NULL REFERENCES client(id),
    service_id BIGINT NOT NULL REFERENCES app_service(id),
    amount NUMERIC(12,2) NOT NULL,
    paid_at DATE NOT NULL,
    collector_id BIGINT REFERENCES app_user(id),
    tp_payment_method BIGINT NOT NULL REFERENCES adm_typology(typology_id),
    tp_payment_status BIGINT NOT NULL REFERENCES adm_typology(typology_id),
    comment VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);
