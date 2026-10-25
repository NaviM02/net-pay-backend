-- Optional idempotency metadata for imported payments; public URLs still use hash_id.
ALTER TABLE payment ADD COLUMN import_key VARCHAR(150);
CREATE UNIQUE INDEX uq_payment_import_key ON payment (import_key) WHERE import_key IS NOT NULL;

-- Imports resolve exact, case-insensitive names/emails, without loading unrelated entities.
CREATE INDEX idx_client_import_name ON client (lower(btrim(business_name))) WHERE deleted_at IS NULL;
CREATE INDEX idx_client_import_email ON client (lower(btrim(contact_email))) WHERE deleted_at IS NULL;
CREATE INDEX idx_client_import_phone ON client (btrim(contact_phone)) WHERE deleted_at IS NULL;
CREATE INDEX idx_user_import_email ON app_user (lower(btrim(email))) WHERE deleted_at IS NULL;
CREATE INDEX idx_user_import_name ON app_user (lower(btrim(full_name))) WHERE deleted_at IS NULL;
