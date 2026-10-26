-- CLIENT CHARGE TABLE (monthly obligation per client)
CREATE TABLE client_charge (
    id BIGSERIAL PRIMARY KEY,
    hash_id VARCHAR(50) NOT NULL UNIQUE,
    client_id BIGINT NOT NULL REFERENCES client(id),
    period_id BIGINT NOT NULL REFERENCES billing_period(id),
    service_id BIGINT NOT NULL REFERENCES app_service(id),
    expected_amount NUMERIC(12,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT uq_client_charge_client_period UNIQUE (client_id, period_id)
);

CREATE INDEX idx_client_charge_client ON client_charge(client_id);
CREATE INDEX idx_client_charge_period ON client_charge(period_id);
