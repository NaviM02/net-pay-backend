INSERT INTO app_permission(hash_id, resource, action)
VALUES (gen_random_uuid()::text, 'my-receipts', 'export');

-- Preserve printing access for every role that already has receipt export.
INSERT INTO app_role_permission(role_id, permission_id)
SELECT rp.role_id, personal.id
FROM app_role_permission rp
JOIN app_permission existing ON existing.id = rp.permission_id
CROSS JOIN app_permission personal
WHERE existing.resource = 'receipts' AND existing.action = 'export'
AND personal.resource = 'my-receipts' AND personal.action = 'export'
ON CONFLICT DO NOTHING;
