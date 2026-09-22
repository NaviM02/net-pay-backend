-- RECEIPT TABLE
CREATE SEQUENCE receipt_number_seq START 1;

CREATE TABLE receipt (
    id BIGSERIAL PRIMARY KEY,
    hash_id VARCHAR(50) NOT NULL UNIQUE,
    receipt_number VARCHAR(30) NOT NULL UNIQUE,
    payment_id BIGINT NOT NULL UNIQUE REFERENCES payment(id),
    client_id BIGINT NOT NULL REFERENCES client(id),
    period_id BIGINT REFERENCES billing_period(id),
    amount NUMERIC(12,2) NOT NULL,
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    collector_id BIGINT REFERENCES app_user(id),
    document_path VARCHAR(500),
    public_id VARCHAR(255),
    printed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_receipt_client ON receipt(client_id);
CREATE INDEX idx_receipt_issued ON receipt(issued_at);
