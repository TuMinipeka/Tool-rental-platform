-- Demo data for local development only.
-- Apply Flyway migrations first, then execute this file once with psql.

BEGIN;

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

INSERT INTO users (
    user_id, email, display_name, password_hash, account_status, registered_at
) VALUES (
    '00000000-0000-0000-0000-000000000001',
    'alex.rivera@example.test',
    'Alex Rivera',
    'SEED_ONLY_NOT_A_REAL_PASSWORD_HASH_01',
    'ACTIVE',
    '2025-11-01T14:00:00Z'
);

INSERT INTO users (
    user_id, email, display_name, password_hash, account_status, registered_at
) VALUES (
    '00000000-0000-0000-0000-000000000002',
    'blair.morgan@example.test',
    'Blair Morgan',
    'SEED_ONLY_NOT_A_REAL_PASSWORD_HASH_02',
    'ACTIVE',
    '2025-11-02T14:00:00Z'
);

INSERT INTO users (
    user_id, email, display_name, password_hash, account_status, registered_at
) VALUES (
    '00000000-0000-0000-0000-000000000003',
    'casey.taylor@example.test',
    'Casey Taylor',
    'SEED_ONLY_NOT_A_REAL_PASSWORD_HASH_03',
    'ACTIVE',
    '2025-11-03T14:00:00Z'
);

INSERT INTO users (
    user_id, email, display_name, password_hash, account_status, registered_at
) VALUES (
    '00000000-0000-0000-0000-000000000004',
    'devon.carter@example.test',
    'Devon Carter',
    'SEED_ONLY_NOT_A_REAL_PASSWORD_HASH_04',
    'ACTIVE',
    '2025-11-04T14:00:00Z'
);

INSERT INTO users (
    user_id, email, display_name, password_hash, account_status, registered_at
) VALUES (
    '00000000-0000-0000-0000-000000000005',
    'ellis.parker@example.test',
    'Ellis Parker',
    'SEED_ONLY_NOT_A_REAL_PASSWORD_HASH_05',
    'ACTIVE',
    '2025-11-05T14:00:00Z'
);

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

INSERT INTO customers (
    user_id, person_type, document_type, document_number, billing_name
) VALUES (
    '00000000-0000-0000-0000-000000000001', 'PERSON', 'CC', '1000000001', 'Alex Rivera'
);

INSERT INTO customers (
    user_id, person_type, document_type, document_number, billing_name
) VALUES (
    '00000000-0000-0000-0000-000000000002', 'PERSON', 'CC', '1000000002', 'Blair Morgan'
);

INSERT INTO customers (
    user_id, person_type, document_type, document_number, billing_name
) VALUES (
    '00000000-0000-0000-0000-000000000003', 'PERSON', 'CC', '1000000003', 'Casey Taylor'
);

INSERT INTO customers (
    user_id, person_type, document_type, document_number, billing_name
) VALUES (
    '00000000-0000-0000-0000-000000000004', 'PERSON', 'CC', '1000000004', 'Devon Carter'
);

INSERT INTO customers (
    user_id, person_type, document_type, document_number, billing_name
) VALUES (
    '00000000-0000-0000-0000-000000000005', 'PERSON', 'CC', '1000000005', 'Ellis Parker'
);

INSERT INTO suppliers (
    user_id, document_type, document_number, business_name
) VALUES (
    '00000000-0000-0000-0000-000000000001', 'NIT', '900000001-1', 'Rivera Tool Supply'
);

INSERT INTO suppliers (
    user_id, document_type, document_number, business_name
) VALUES (
    '00000000-0000-0000-0000-000000000002', 'NIT', '900000002-2', 'Morgan Equipment'
);

INSERT INTO suppliers (
    user_id, document_type, document_number, business_name
) VALUES (
    '00000000-0000-0000-0000-000000000003', 'NIT', '900000003-3', 'Taylor Construction Tools'
);

INSERT INTO suppliers (
    user_id, document_type, document_number, business_name
) VALUES (
    '00000000-0000-0000-0000-000000000004', 'NIT', '900000004-4', 'Carter Rental Services'
);

INSERT INTO suppliers (
    user_id, document_type, document_number, business_name
) VALUES (
    '00000000-0000-0000-0000-000000000005', 'NIT', '900000005-5', 'Parker Industrial Rentals'
);

INSERT INTO categories (category_id, name, description)
VALUES (
    '10000000-0000-0000-0000-000000000001',
    'Power Tools',
    'Electric and battery-powered tools'
);

INSERT INTO categories (category_id, name, description)
VALUES (
    '10000000-0000-0000-0000-000000000002',
    'Gardening',
    'Tools for gardens and outdoor maintenance'
);

INSERT INTO categories (category_id, name, description)
VALUES (
    '10000000-0000-0000-0000-000000000003',
    'Construction',
    'Heavy-duty construction equipment'
);

INSERT INTO categories (category_id, name, description)
VALUES (
    '10000000-0000-0000-0000-000000000004',
    'Safety',
    'Protective and job-site safety equipment'
);

INSERT INTO categories (category_id, name, description)
VALUES (
    '10000000-0000-0000-0000-000000000005',
    'Cleaning',
    'Industrial cleaning tools and equipment'
);

INSERT INTO tools (
    tool_id, supplier_id, category_id, name, description,
    current_daily_rate, rate_currency, publication_status
) VALUES (
    '20000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000001',
    '10000000-0000-0000-0000-000000000001',
    'Rotary Hammer',
    'Professional rotary hammer for concrete drilling',
    65000.0000,
    'COP',
    'PUBLISHED'
);

INSERT INTO tools (
    tool_id, supplier_id, category_id, name, description,
    current_daily_rate, rate_currency, publication_status
) VALUES (
    '20000000-0000-0000-0000-000000000002',
    '00000000-0000-0000-0000-000000000002',
    '10000000-0000-0000-0000-000000000002',
    'Brush Cutter',
    'Gas-powered brush cutter for dense vegetation',
    55000.0000,
    'COP',
    'PUBLISHED'
);

INSERT INTO tools (
    tool_id, supplier_id, category_id, name, description,
    current_daily_rate, rate_currency, publication_status
) VALUES (
    '20000000-0000-0000-0000-000000000003',
    '00000000-0000-0000-0000-000000000003',
    '10000000-0000-0000-0000-000000000003',
    'Concrete Mixer',
    'Portable concrete mixer with a 120-liter drum',
    75000.0000,
    'COP',
    'PUBLISHED'
);

INSERT INTO tools (
    tool_id, supplier_id, category_id, name, description,
    current_daily_rate, rate_currency, publication_status
) VALUES (
    '20000000-0000-0000-0000-000000000004',
    '00000000-0000-0000-0000-000000000004',
    '10000000-0000-0000-0000-000000000004',
    'Safety Harness Kit',
    'Complete fall-protection harness kit',
    30000.0000,
    'COP',
    'PUBLISHED'
);

INSERT INTO tools (
    tool_id, supplier_id, category_id, name, description,
    current_daily_rate, rate_currency, publication_status
) VALUES (
    '20000000-0000-0000-0000-000000000005',
    '00000000-0000-0000-0000-000000000005',
    '10000000-0000-0000-0000-000000000005',
    'Pressure Washer',
    'Industrial pressure washer rated at 2600 PSI',
    48000.0000,
    'COP',
    'PUBLISHED'
);

INSERT INTO tool_images (tool_id, image_url)
VALUES (
    '20000000-0000-0000-0000-000000000001',
    'https://example.test/images/rotary-hammer.jpg'
);

INSERT INTO tool_images (tool_id, image_url)
VALUES (
    '20000000-0000-0000-0000-000000000002',
    'https://example.test/images/brush-cutter.jpg'
);

INSERT INTO tool_images (tool_id, image_url)
VALUES (
    '20000000-0000-0000-0000-000000000003',
    'https://example.test/images/concrete-mixer.jpg'
);

INSERT INTO tool_images (tool_id, image_url)
VALUES (
    '20000000-0000-0000-0000-000000000004',
    'https://example.test/images/safety-harness.jpg'
);

INSERT INTO tool_images (tool_id, image_url)
VALUES (
    '20000000-0000-0000-0000-000000000005',
    'https://example.test/images/pressure-washer.jpg'
);

INSERT INTO equipment_units (
    equipment_unit_id, tool_id, inventory_code, serial_number,
    current_condition, operational_status
) VALUES (
    '30000000-0000-0000-0000-000000000001',
    '20000000-0000-0000-0000-000000000001',
    'RIV-HAM-001',
    'RH-2026-001',
    'GOOD',
    'OPERATIONAL'
);

INSERT INTO equipment_units (
    equipment_unit_id, tool_id, inventory_code, serial_number,
    current_condition, operational_status
) VALUES (
    '30000000-0000-0000-0000-000000000002',
    '20000000-0000-0000-0000-000000000002',
    'MOR-BRU-001',
    'BC-2026-002',
    'GOOD',
    'OPERATIONAL'
);

INSERT INTO equipment_units (
    equipment_unit_id, tool_id, inventory_code, serial_number,
    current_condition, operational_status
) VALUES (
    '30000000-0000-0000-0000-000000000003',
    '20000000-0000-0000-0000-000000000003',
    'TAY-MIX-001',
    'CM-2026-003',
    'GOOD',
    'OPERATIONAL'
);

INSERT INTO equipment_units (
    equipment_unit_id, tool_id, inventory_code, serial_number,
    current_condition, operational_status
) VALUES (
    '30000000-0000-0000-0000-000000000004',
    '20000000-0000-0000-0000-000000000004',
    'CAR-SAF-001',
    'SH-2026-004',
    'GOOD',
    'OPERATIONAL'
);

INSERT INTO equipment_units (
    equipment_unit_id, tool_id, inventory_code, serial_number,
    current_condition, operational_status
) VALUES (
    '30000000-0000-0000-0000-000000000005',
    '20000000-0000-0000-0000-000000000005',
    'PAR-WAS-001',
    'PW-2026-005',
    'GOOD',
    'OPERATIONAL'
);

INSERT INTO reservations (
    reservation_id, customer_id, created_at, start_date, return_date,
    reservation_status, currency, confirmed_at
) VALUES (
    '40000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000001',
    '2026-01-05T14:00:00Z',
    '2026-01-10',
    '2026-01-12',
    'CONFIRMED',
    'COP',
    '2026-01-05T15:00:00Z'
);

INSERT INTO reservations (
    reservation_id, customer_id, created_at, start_date, return_date,
    reservation_status, currency, confirmed_at
) VALUES (
    '40000000-0000-0000-0000-000000000002',
    '00000000-0000-0000-0000-000000000002',
    '2026-02-03T14:00:00Z',
    '2026-02-10',
    '2026-02-13',
    'CONFIRMED',
    'COP',
    '2026-02-03T15:00:00Z'
);

INSERT INTO reservations (
    reservation_id, customer_id, created_at, start_date, return_date,
    reservation_status, currency, confirmed_at
) VALUES (
    '40000000-0000-0000-0000-000000000003',
    '00000000-0000-0000-0000-000000000003',
    '2026-03-01T14:00:00Z',
    '2026-03-05',
    '2026-03-06',
    'CONFIRMED',
    'COP',
    '2026-03-01T15:00:00Z'
);

INSERT INTO reservations (
    reservation_id, customer_id, created_at, start_date, return_date,
    reservation_status, currency, confirmed_at
) VALUES (
    '40000000-0000-0000-0000-000000000004',
    '00000000-0000-0000-0000-000000000004',
    '2026-03-25T14:00:00Z',
    '2026-04-01',
    '2026-04-05',
    'CONFIRMED',
    'COP',
    '2026-03-25T15:00:00Z'
);

INSERT INTO reservations (
    reservation_id, customer_id, created_at, start_date, return_date,
    reservation_status, currency, confirmed_at
) VALUES (
    '40000000-0000-0000-0000-000000000005',
    '00000000-0000-0000-0000-000000000005',
    '2026-05-15T14:00:00Z',
    '2026-05-20',
    '2026-05-21',
    'CONFIRMED',
    'COP',
    '2026-05-15T15:00:00Z'
);

INSERT INTO reservation_items (reservation_id, equipment_unit_id, agreed_daily_rate)
VALUES (
    '40000000-0000-0000-0000-000000000001',
    '30000000-0000-0000-0000-000000000002',
    55000.0000
);

INSERT INTO reservation_items (reservation_id, equipment_unit_id, agreed_daily_rate)
VALUES (
    '40000000-0000-0000-0000-000000000002',
    '30000000-0000-0000-0000-000000000003',
    75000.0000
);

INSERT INTO reservation_items (reservation_id, equipment_unit_id, agreed_daily_rate)
VALUES (
    '40000000-0000-0000-0000-000000000003',
    '30000000-0000-0000-0000-000000000004',
    30000.0000
);

INSERT INTO reservation_items (reservation_id, equipment_unit_id, agreed_daily_rate)
VALUES (
    '40000000-0000-0000-0000-000000000004',
    '30000000-0000-0000-0000-000000000005',
    48000.0000
);

INSERT INTO reservation_items (reservation_id, equipment_unit_id, agreed_daily_rate)
VALUES (
    '40000000-0000-0000-0000-000000000005',
    '30000000-0000-0000-0000-000000000001',
    65000.0000
);

INSERT INTO rentals (reservation_id, started_at, rental_status)
VALUES ('40000000-0000-0000-0000-000000000001', '2026-01-10T13:00:00Z', 'COMPLETED');

INSERT INTO rentals (reservation_id, started_at, rental_status)
VALUES ('40000000-0000-0000-0000-000000000002', '2026-02-10T13:00:00Z', 'COMPLETED');

INSERT INTO rentals (reservation_id, started_at, rental_status)
VALUES ('40000000-0000-0000-0000-000000000003', '2026-03-05T13:00:00Z', 'COMPLETED');

INSERT INTO rentals (reservation_id, started_at, rental_status)
VALUES ('40000000-0000-0000-0000-000000000004', '2026-04-01T13:00:00Z', 'COMPLETED');

INSERT INTO rentals (reservation_id, started_at, rental_status)
VALUES ('40000000-0000-0000-0000-000000000005', '2026-05-20T13:00:00Z', 'COMPLETED');

INSERT INTO unit_deliveries (
    reservation_id, equipment_unit_id, scheduled_at, delivered_at,
    outbound_condition, address_line, city, region, country
) VALUES (
    '40000000-0000-0000-0000-000000000001',
    '30000000-0000-0000-0000-000000000002',
    '2026-01-10T13:00:00Z',
    '2026-01-10T13:15:00Z',
    'Good condition',
    '101 First Avenue',
    'Bogota',
    'Bogota D.C.',
    'Colombia'
);

INSERT INTO unit_deliveries (
    reservation_id, equipment_unit_id, scheduled_at, delivered_at,
    outbound_condition, address_line, city, region, country
) VALUES (
    '40000000-0000-0000-0000-000000000002',
    '30000000-0000-0000-0000-000000000003',
    '2026-02-10T13:00:00Z',
    '2026-02-10T13:20:00Z',
    'Good condition',
    '202 Second Avenue',
    'Medellin',
    'Antioquia',
    'Colombia'
);

INSERT INTO unit_deliveries (
    reservation_id, equipment_unit_id, scheduled_at, delivered_at,
    outbound_condition, address_line, city, region, country
) VALUES (
    '40000000-0000-0000-0000-000000000003',
    '30000000-0000-0000-0000-000000000004',
    '2026-03-05T13:00:00Z',
    '2026-03-05T13:10:00Z',
    'Good condition',
    '303 Third Avenue',
    'Cali',
    'Valle del Cauca',
    'Colombia'
);

INSERT INTO unit_deliveries (
    reservation_id, equipment_unit_id, scheduled_at, delivered_at,
    outbound_condition, address_line, city, region, country
) VALUES (
    '40000000-0000-0000-0000-000000000004',
    '30000000-0000-0000-0000-000000000005',
    '2026-04-01T13:00:00Z',
    '2026-04-01T13:25:00Z',
    'Good condition',
    '404 Fourth Avenue',
    'Barranquilla',
    'Atlantico',
    'Colombia'
);

INSERT INTO unit_deliveries (
    reservation_id, equipment_unit_id, scheduled_at, delivered_at,
    outbound_condition, address_line, city, region, country
) VALUES (
    '40000000-0000-0000-0000-000000000005',
    '30000000-0000-0000-0000-000000000001',
    '2026-05-20T13:00:00Z',
    '2026-05-20T13:30:00Z',
    'Good condition',
    '505 Fifth Avenue',
    'Cartagena',
    'Bolivar',
    'Colombia'
);

INSERT INTO unit_returns (
    reservation_id, equipment_unit_id, received_at, inbound_condition, notes
) VALUES (
    '40000000-0000-0000-0000-000000000001',
    '30000000-0000-0000-0000-000000000002',
    '2026-01-12T22:00:00Z',
    'Good condition',
    'Returned clean and complete'
);

INSERT INTO unit_returns (
    reservation_id, equipment_unit_id, received_at, inbound_condition, notes
) VALUES (
    '40000000-0000-0000-0000-000000000002',
    '30000000-0000-0000-0000-000000000003',
    '2026-02-13T22:00:00Z',
    'Minor cosmetic wear',
    'Small paint scratch on the frame'
);

INSERT INTO unit_returns (
    reservation_id, equipment_unit_id, received_at, inbound_condition, notes
) VALUES (
    '40000000-0000-0000-0000-000000000003',
    '30000000-0000-0000-0000-000000000004',
    '2026-03-06T22:00:00Z',
    'Good condition',
    'Returned with all accessories'
);

INSERT INTO unit_returns (
    reservation_id, equipment_unit_id, received_at, inbound_condition, notes
) VALUES (
    '40000000-0000-0000-0000-000000000004',
    '30000000-0000-0000-0000-000000000005',
    '2026-04-05T22:00:00Z',
    'Requires inspection',
    'Pressure hose shows visible wear'
);

INSERT INTO unit_returns (
    reservation_id, equipment_unit_id, received_at, inbound_condition, notes
) VALUES (
    '40000000-0000-0000-0000-000000000005',
    '30000000-0000-0000-0000-000000000001',
    '2026-05-21T22:00:00Z',
    'Good condition',
    'Returned clean and operational'
);

INSERT INTO damage_reports (
    damage_report_id, reservation_id, equipment_unit_id, reported_at,
    description, report_status, assessed_cost
) VALUES (
    '50000000-0000-0000-0000-000000000001',
    '40000000-0000-0000-0000-000000000001',
    '30000000-0000-0000-0000-000000000002',
    '2026-01-12T22:15:00Z',
    'No chargeable damage found',
    'CLOSED',
    0.0000
);

INSERT INTO damage_reports (
    damage_report_id, reservation_id, equipment_unit_id, reported_at,
    description, report_status, assessed_cost
) VALUES (
    '50000000-0000-0000-0000-000000000002',
    '40000000-0000-0000-0000-000000000002',
    '30000000-0000-0000-0000-000000000003',
    '2026-02-13T22:15:00Z',
    'Cosmetic frame scratch',
    'CLOSED',
    15000.0000
);

INSERT INTO damage_reports (
    damage_report_id, reservation_id, equipment_unit_id, reported_at,
    description, report_status, assessed_cost
) VALUES (
    '50000000-0000-0000-0000-000000000003',
    '40000000-0000-0000-0000-000000000003',
    '30000000-0000-0000-0000-000000000004',
    '2026-03-06T22:15:00Z',
    'No chargeable damage found',
    'CLOSED',
    0.0000
);

INSERT INTO damage_reports (
    damage_report_id, reservation_id, equipment_unit_id, reported_at,
    description, report_status, assessed_cost
) VALUES (
    '50000000-0000-0000-0000-000000000004',
    '40000000-0000-0000-0000-000000000004',
    '30000000-0000-0000-0000-000000000005',
    '2026-04-05T22:15:00Z',
    'Pressure hose replacement required',
    'CLOSED',
    45000.0000
);

INSERT INTO damage_reports (
    damage_report_id, reservation_id, equipment_unit_id, reported_at,
    description, report_status, assessed_cost
) VALUES (
    '50000000-0000-0000-0000-000000000005',
    '40000000-0000-0000-0000-000000000005',
    '30000000-0000-0000-0000-000000000001',
    '2026-05-21T22:15:00Z',
    'No chargeable damage found',
    'CLOSED',
    0.0000
);

INSERT INTO payments (
    payment_id, reservation_id, processor, external_reference, amount,
    payment_status, recorded_at, confirmed_at
) VALUES (
    '60000000-0000-0000-0000-000000000001',
    '40000000-0000-0000-0000-000000000001',
    'SEED_GATEWAY',
    'SEED-PAY-0001',
    165000.0000,
    'CONFIRMED',
    '2026-01-05T14:30:00Z',
    '2026-01-05T14:31:00Z'
);

INSERT INTO payments (
    payment_id, reservation_id, processor, external_reference, amount,
    payment_status, recorded_at, confirmed_at
) VALUES (
    '60000000-0000-0000-0000-000000000002',
    '40000000-0000-0000-0000-000000000002',
    'SEED_GATEWAY',
    'SEED-PAY-0002',
    300000.0000,
    'CONFIRMED',
    '2026-02-03T14:30:00Z',
    '2026-02-03T14:31:00Z'
);

INSERT INTO payments (
    payment_id, reservation_id, processor, external_reference, amount,
    payment_status, recorded_at, confirmed_at
) VALUES (
    '60000000-0000-0000-0000-000000000003',
    '40000000-0000-0000-0000-000000000003',
    'SEED_GATEWAY',
    'SEED-PAY-0003',
    60000.0000,
    'CONFIRMED',
    '2026-03-01T14:30:00Z',
    '2026-03-01T14:31:00Z'
);

INSERT INTO payments (
    payment_id, reservation_id, processor, external_reference, amount,
    payment_status, recorded_at, confirmed_at
) VALUES (
    '60000000-0000-0000-0000-000000000004',
    '40000000-0000-0000-0000-000000000004',
    'SEED_GATEWAY',
    'SEED-PAY-0004',
    240000.0000,
    'CONFIRMED',
    '2026-03-25T14:30:00Z',
    '2026-03-25T14:31:00Z'
);

INSERT INTO payments (
    payment_id, reservation_id, processor, external_reference, amount,
    payment_status, recorded_at, confirmed_at
) VALUES (
    '60000000-0000-0000-0000-000000000005',
    '40000000-0000-0000-0000-000000000005',
    'SEED_GATEWAY',
    'SEED-PAY-0005',
    130000.0000,
    'CONFIRMED',
    '2026-05-15T14:30:00Z',
    '2026-05-15T14:31:00Z'
);

INSERT INTO invoices (
    invoice_id, reservation_id, number, issued_at, invoice_status,
    issued_supplier_name, issued_supplier_document,
    issued_customer_name, issued_customer_document
) VALUES (
    '70000000-0000-0000-0000-000000000001',
    '40000000-0000-0000-0000-000000000001',
    'INV-SEED-0001',
    '2026-01-12T22:30:00Z',
    'PAID',
    'Morgan Equipment',
    '900000002-2',
    'Alex Rivera',
    '1000000001'
);

INSERT INTO invoices (
    invoice_id, reservation_id, number, issued_at, invoice_status,
    issued_supplier_name, issued_supplier_document,
    issued_customer_name, issued_customer_document
) VALUES (
    '70000000-0000-0000-0000-000000000002',
    '40000000-0000-0000-0000-000000000002',
    'INV-SEED-0002',
    '2026-02-13T22:30:00Z',
    'PAID',
    'Taylor Construction Tools',
    '900000003-3',
    'Blair Morgan',
    '1000000002'
);

INSERT INTO invoices (
    invoice_id, reservation_id, number, issued_at, invoice_status,
    issued_supplier_name, issued_supplier_document,
    issued_customer_name, issued_customer_document
) VALUES (
    '70000000-0000-0000-0000-000000000003',
    '40000000-0000-0000-0000-000000000003',
    'INV-SEED-0003',
    '2026-03-06T22:30:00Z',
    'PAID',
    'Carter Rental Services',
    '900000004-4',
    'Casey Taylor',
    '1000000003'
);

INSERT INTO invoices (
    invoice_id, reservation_id, number, issued_at, invoice_status,
    issued_supplier_name, issued_supplier_document,
    issued_customer_name, issued_customer_document
) VALUES (
    '70000000-0000-0000-0000-000000000004',
    '40000000-0000-0000-0000-000000000004',
    'INV-SEED-0004',
    '2026-04-05T22:30:00Z',
    'PAID',
    'Parker Industrial Rentals',
    '900000005-5',
    'Devon Carter',
    '1000000004'
);

INSERT INTO invoices (
    invoice_id, reservation_id, number, issued_at, invoice_status,
    issued_supplier_name, issued_supplier_document,
    issued_customer_name, issued_customer_document
) VALUES (
    '70000000-0000-0000-0000-000000000005',
    '40000000-0000-0000-0000-000000000005',
    'INV-SEED-0005',
    '2026-05-21T22:30:00Z',
    'PAID',
    'Rivera Tool Supply',
    '900000001-1',
    'Ellis Parker',
    '1000000005'
);

INSERT INTO invoice_items (
    invoice_id, line_number, issued_description, quantity, issued_unit_price
) VALUES (
    '70000000-0000-0000-0000-000000000001',
    1,
    'Brush Cutter rental',
    3.0000,
    55000.0000
);

INSERT INTO invoice_items (
    invoice_id, line_number, issued_description, quantity, issued_unit_price
) VALUES (
    '70000000-0000-0000-0000-000000000002',
    1,
    'Concrete Mixer rental',
    4.0000,
    75000.0000
);

INSERT INTO invoice_items (
    invoice_id, line_number, issued_description, quantity, issued_unit_price
) VALUES (
    '70000000-0000-0000-0000-000000000003',
    1,
    'Safety Harness Kit rental',
    2.0000,
    30000.0000
);

INSERT INTO invoice_items (
    invoice_id, line_number, issued_description, quantity, issued_unit_price
) VALUES (
    '70000000-0000-0000-0000-000000000004',
    1,
    'Pressure Washer rental',
    5.0000,
    48000.0000
);

INSERT INTO invoice_items (
    invoice_id, line_number, issued_description, quantity, issued_unit_price
) VALUES (
    '70000000-0000-0000-0000-000000000005',
    1,
    'Rotary Hammer rental',
    2.0000,
    65000.0000
);

COMMIT;
