-- Local demonstration data. Loaded only when the demo profile is explicitly active.
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
