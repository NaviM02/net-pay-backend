-- BILLING PERIOD TABLE
CREATE TABLE billing_period (
    id BIGSERIAL PRIMARY KEY,
    year INTEGER NOT NULL,
    month INTEGER NOT NULL,
    starts_on DATE NOT NULL,
    ends_on DATE NOT NULL,
    is_closed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_billing_period_year_month UNIQUE (year, month),
    CONSTRAINT ck_billing_period_month CHECK (month BETWEEN 1 AND 12)
);
