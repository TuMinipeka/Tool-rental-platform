-- Local demonstration data. Loaded only when the local profile is active.
INSERT INTO roles (code, description)
VALUES ('CUSTOMER', 'Customer profile permissions')
ON CONFLICT (code) DO NOTHING;

INSERT INTO roles (code, description)
VALUES ('SUPPLIER', 'Supplier profile permissions')
ON CONFLICT (code) DO NOTHING;

INSERT INTO roles (code, description)
VALUES ('ADMIN', 'Platform administration permissions')
ON CONFLICT (code) DO NOTHING;

INSERT INTO roles (code, description)
VALUES ('SUPPORT', 'Customer support permissions')
ON CONFLICT (code) DO NOTHING;

INSERT INTO roles (code, description)
VALUES ('OPERATIONS', 'Rental operations permissions')
ON CONFLICT (code) DO NOTHING;
