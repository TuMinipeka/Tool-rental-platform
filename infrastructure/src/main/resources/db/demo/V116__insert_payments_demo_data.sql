-- Local demonstration data. Loaded only when the local profile is active.
INSERT INTO payments (
    payment_id, reservation_id, processor, external_reference, amount,
    payment_status, recorded_at, confirmed_at
) VALUES (
    '60000000-0000-0000-0000-000000000001',
    '40000000-0000-0000-0000-000000000001',
    'SEED_GATEWAY',
    'SEED-PAY-0001',
    60000.0000,
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
    40000.0000,
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
    100000.0000,
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
    300000.0000,
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
    20000.0000,
    'CONFIRMED',
    '2026-05-15T14:30:00Z',
    '2026-05-15T14:31:00Z'
);
