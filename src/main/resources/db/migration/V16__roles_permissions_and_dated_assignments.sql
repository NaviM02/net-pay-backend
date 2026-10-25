CREATE TABLE app_permission (
    id BIGSERIAL PRIMARY KEY,
    hash_id VARCHAR(50) NOT NULL UNIQUE,
    resource VARCHAR(50) NOT NULL,
    action VARCHAR(20) NOT NULL CHECK (action IN ('read', 'create', 'update', 'delete', 'export', 'import')),
    UNIQUE (resource, action)
);

CREATE TABLE app_role (
    id BIGSERIAL PRIMARY KEY,
    hash_id VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'DELETED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX uq_app_role_name ON app_role (lower(name)) WHERE status <> 'DELETED';

CREATE TABLE app_role_permission (
    role_id BIGINT NOT NULL REFERENCES app_role(id),
    permission_id BIGINT NOT NULL REFERENCES app_permission(id),
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE app_user_role (
    id BIGSERIAL PRIMARY KEY,
    hash_id VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES app_user(id),
    role_id BIGINT NOT NULL REFERENCES app_role(id),
    starts_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ends_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (ends_at IS NULL OR ends_at > starts_at)
);
CREATE INDEX idx_app_user_role_effective ON app_user_role(user_id, starts_at, ends_at) WHERE cancelled_at IS NULL;
CREATE INDEX idx_app_user_role_role ON app_user_role(role_id);

-- Only actions supported by the current API are offered in the catalogue.
INSERT INTO app_permission(hash_id, resource, action)
SELECT gen_random_uuid()::text, resource, action
FROM (VALUES ('users'), ('roles'), ('services'), ('clients'), ('payments')) r(resource)
CROSS JOIN (VALUES ('read'), ('create'), ('update'), ('delete')) a(action);
INSERT INTO app_permission(hash_id, resource, action)
SELECT gen_random_uuid()::text, resource, action FROM (VALUES
    ('users', 'import'), ('services', 'import'), ('clients', 'import'), ('payments', 'import'),
    ('reports', 'read'), ('reports', 'export'), ('dashboard', 'read'),
    ('receipts', 'read'), ('receipts', 'export'), ('audit-logs', 'read'),
    ('typologies', 'read'), ('permissions', 'read')
) p(resource, action);

INSERT INTO app_role(hash_id, name, description) VALUES
('bb66bb4f-020e-44de-b604-8a3153d74861', 'Administrador', 'Administración de NetPay Manager'),
('bb66bb4f-020e-44de-b604-8a3153d74862', 'Cobrador', 'Consulta y registro de cobros');

INSERT INTO app_role_permission(role_id, permission_id)
SELECT r.id, p.id FROM app_role r CROSS JOIN app_permission p
WHERE r.hash_id = 'bb66bb4f-020e-44de-b604-8a3153d74861';
INSERT INTO app_role_permission(role_id, permission_id)
SELECT r.id, p.id FROM app_role r CROSS JOIN app_permission p
WHERE r.hash_id = 'bb66bb4f-020e-44de-b604-8a3153d74862'
AND ((p.action = 'read' AND p.resource IN ('clients', 'services', 'payments', 'receipts', 'typologies', 'dashboard'))
     OR (p.resource = 'payments' AND p.action = 'create')
     OR (p.resource = 'receipts' AND p.action = 'export'));

-- Preserve existing assignments before retiring the typology-based role column.
INSERT INTO app_user_role(hash_id, user_id, role_id)
SELECT gen_random_uuid()::text, u.id, r.id
FROM app_user u JOIN adm_typology t ON t.typology_id = u.tp_role
JOIN app_role r ON r.hash_id = CASE t.internal_id
    WHEN 61 THEN 'bb66bb4f-020e-44de-b604-8a3153d74861'
    WHEN 62 THEN 'bb66bb4f-020e-44de-b604-8a3153d74862' END;
ALTER TABLE app_user DROP COLUMN tp_role;
