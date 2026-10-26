-- Reuse the former lookup permission for the personal dashboard page.
UPDATE app_permission SET resource = 'my-dashboard'
WHERE resource = 'typologies' AND action = 'read';

INSERT INTO app_permission(hash_id, resource, action)
SELECT gen_random_uuid()::text, resource, action FROM (VALUES
    ('my-clients', 'read'),
    ('my-payments', 'read'),
    ('my-payments', 'create'),
    ('my-receipts', 'read')
) p(resource, action);

-- Default roles are seed data; runtime authorization only evaluates permissions.
INSERT INTO app_role_permission(role_id, permission_id)
SELECT r.id, p.id FROM app_role r CROSS JOIN app_permission p
WHERE r.hash_id IN ('bb66bb4f-020e-44de-b604-8a3153d74861', 'bb66bb4f-020e-44de-b604-8a3153d74862')
AND p.resource IN ('my-dashboard', 'my-clients', 'my-payments', 'my-receipts')
ON CONFLICT DO NOTHING;

-- The personal pages replace the equivalent general-page grants of this seed.
DELETE FROM app_role_permission rp USING app_role r, app_permission p
WHERE rp.role_id = r.id AND rp.permission_id = p.id
AND r.hash_id = 'bb66bb4f-020e-44de-b604-8a3153d74862'
AND ((p.action = 'read' AND p.resource IN ('dashboard', 'clients', 'payments', 'receipts'))
     OR (p.resource = 'payments' AND p.action = 'create'));
