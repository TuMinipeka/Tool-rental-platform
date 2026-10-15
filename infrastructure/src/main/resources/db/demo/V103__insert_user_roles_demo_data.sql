-- Local demonstration data. Loaded only when the demo profile is explicitly active.
INSERT INTO user_roles (user_id, role_id)
SELECT '00000000-0000-0000-0000-000000000001', role_id
FROM roles
WHERE code IN ('CUSTOMER', 'SUPPLIER');

INSERT INTO user_roles (user_id, role_id)
SELECT '00000000-0000-0000-0000-000000000002', role_id
FROM roles
WHERE code IN ('CUSTOMER', 'SUPPLIER');

INSERT INTO user_roles (user_id, role_id)
SELECT '00000000-0000-0000-0000-000000000003', role_id
FROM roles
WHERE code IN ('CUSTOMER', 'SUPPLIER');

INSERT INTO user_roles (user_id, role_id)
SELECT '00000000-0000-0000-0000-000000000004', role_id
FROM roles
WHERE code IN ('CUSTOMER', 'SUPPLIER');

INSERT INTO user_roles (user_id, role_id)
SELECT '00000000-0000-0000-0000-000000000005', role_id
FROM roles
WHERE code IN ('CUSTOMER', 'SUPPLIER');
