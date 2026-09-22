-- AUDIT LOG TABLE
CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES app_user(id),
    user_email VARCHAR(150),
    action VARCHAR(100) NOT NULL,
    detail TEXT,
    entity_type VARCHAR(50),
    entity_hash_id VARCHAR(50),
    changes JSONB,
    ip_address VARCHAR(45),
    tp_level BIGINT REFERENCES adm_typology(typology_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_log_entity ON audit_log(entity_type, entity_hash_id);
CREATE INDEX idx_audit_log_created ON audit_log(created_at);
