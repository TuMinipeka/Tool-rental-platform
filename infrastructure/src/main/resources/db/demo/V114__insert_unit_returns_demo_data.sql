-- Local demonstration data. Loaded only when the demo profile is explicitly active.
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
