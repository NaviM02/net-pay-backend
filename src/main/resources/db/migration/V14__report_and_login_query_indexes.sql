-- Date-range aggregates and per-collector reports over non-deleted payments.
CREATE INDEX idx_payment_paid_at_alive ON payment (paid_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_payment_collector_paid_at_alive ON payment (collector_id, paid_at) WHERE deleted_at IS NULL;

-- Recent payments use created_at, including their existing status filter.
CREATE INDEX idx_payment_created_at ON payment (created_at DESC);

-- Batch service counts and scoped charge synchronization / overdue reports.
CREATE INDEX idx_client_service_status ON client (service_id, tp_status);
CREATE INDEX idx_client_collector_alive ON client (collector_id) WHERE deleted_at IS NULL;

-- V13 removed email uniqueness; login still needs an indexed email/status lookup.
CREATE INDEX idx_app_user_email_status ON app_user (email, tp_status);
