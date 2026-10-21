-- Local demonstration data. Loaded only when the demo profile is explicitly active.
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
