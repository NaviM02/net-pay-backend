-- Roles remain independent entities; only their lifecycle status uses adm_typology.
ALTER TABLE app_role ADD COLUMN tp_status BIGINT REFERENCES adm_typology(typology_id);
UPDATE app_role r SET tp_status = t.typology_id FROM adm_typology t
WHERE t.internal_id = CASE r.status WHEN 'ACTIVE' THEN 49 WHEN 'INACTIVE' THEN 50 WHEN 'DELETED' THEN 51 END;
ALTER TABLE app_role ALTER COLUMN tp_status SET NOT NULL;
DROP INDEX uq_app_role_name;
ALTER TABLE app_role DROP COLUMN status;
CREATE UNIQUE INDEX uq_app_role_name ON app_role(lower(name)) WHERE tp_status <> 30051;

-- LOCKED means active and assignable, but protected against editing/deletion.
UPDATE app_role SET tp_status = (SELECT typology_id FROM adm_typology WHERE internal_id = 52),
    updated_at = CURRENT_TIMESTAMP
WHERE hash_id IN ('bb66bb4f-020e-44de-b604-8a3153d74861', 'bb66bb4f-020e-44de-b604-8a3153d74862');
