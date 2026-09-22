-- PAYMENT LEDGER
ALTER TABLE payment ADD COLUMN reference VARCHAR(255);

-- PAYMENT ALLOCATION TABLE (how much of a payment covers a given monthly charge)
CREATE TABLE payment_allocation (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL REFERENCES payment(id) ON DELETE CASCADE,
    charge_id BIGINT NOT NULL REFERENCES client_charge(id),
    amount NUMERIC(12,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_payment_allocation UNIQUE (payment_id, charge_id),
    CONSTRAINT ck_payment_allocation_amount CHECK (amount > 0)
);

CREATE INDEX idx_payment_allocation_payment ON payment_allocation(payment_id);
CREATE INDEX idx_payment_allocation_charge ON payment_allocation(charge_id);
