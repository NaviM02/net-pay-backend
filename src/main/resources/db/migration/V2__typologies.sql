INSERT INTO adm_typology (typology_id, internal_id, parent_typology_id, description, value_1, value_2)
VALUES (-30, -30, NULL, 'Global', NULL, NULL);

INSERT INTO adm_typology (typology_id, internal_id, parent_typology_id, description, value_1, value_2)
VALUES (30048, 48, -30, 'Status', NULL, NULL);

INSERT INTO adm_typology (typology_id, internal_id, parent_typology_id, description, value_1, value_2) VALUES
(30049, 49, 30048, 'Active', NULL, NULL),
(30050, 50, 30048, 'Inactive', NULL, NULL),
(30051, 51, 30048, 'Deleted', NULL, NULL),
(30052, 52, 30048, 'Locked', NULL, NULL),
(30053, 53, 30048, 'Suspended', NULL, NULL);


INSERT INTO adm_typology (typology_id, internal_id, parent_typology_id, description, value_1, value_2)
VALUES (30060, 60, -30, 'User_Roles', NULL, NULL);

INSERT INTO adm_typology (typology_id, internal_id, parent_typology_id, description, value_1, value_2) VALUES
(30061, 61, 30060, 'Administrator', 'ADMIN', NULL),
(30062, 62, 30060, 'Collector', 'COLLECTOR', NULL);


INSERT INTO adm_typology (typology_id, internal_id, parent_typology_id, description, value_1, value_2)
VALUES (30070, 70, -30, 'Service_Types', NULL, NULL);

INSERT INTO adm_typology (typology_id, internal_id, parent_typology_id, description, value_1, value_2) VALUES
(30071, 71, 30070, 'Residential', 'RESIDENTIAL', NULL),
(30072, 72, 30070, 'Business', 'BUSINESS', NULL),
(30073, 73, 30070, 'Corporate', 'CORPORATE', NULL);


INSERT INTO adm_typology (typology_id, internal_id, parent_typology_id, description, value_1, value_2)
VALUES (30080, 80, -30, 'Payment_Methods', NULL, NULL);

INSERT INTO adm_typology (typology_id, internal_id, parent_typology_id, description, value_1, value_2) VALUES
(30081, 81, 30080, 'Cash', 'CASH', NULL),
(30082, 82, 30080, 'Transfer', 'TRANSFER', NULL),
(30083, 83, 30080, 'Card', 'CARD', NULL);


INSERT INTO adm_typology (typology_id, internal_id, parent_typology_id, description, value_1, value_2)
VALUES (30090, 90, -30, 'Payment_Status', NULL, NULL);

INSERT INTO adm_typology (typology_id, internal_id, parent_typology_id, description, value_1, value_2) VALUES
(30091, 91, 30090, 'Completed', 'COMPLETED', NULL),
(30092, 92, 30090, 'Pending', 'PENDING', NULL),
(30093, 93, 30090, 'Failed', 'FAILED', NULL);


INSERT INTO adm_typology (typology_id, internal_id, parent_typology_id, description, value_1, value_2)
VALUES (30100, 100, -30, 'Audit_Levels', NULL, NULL);

INSERT INTO adm_typology (typology_id, internal_id, parent_typology_id, description, value_1, value_2) VALUES
(30101, 101, 30100, 'Info', 'INFO', NULL),
(30102, 102, 30100, 'Warning', 'WARNING', NULL),
(30103, 103, 30100, 'Error', 'ERROR', NULL);
