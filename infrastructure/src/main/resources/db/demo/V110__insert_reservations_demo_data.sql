-- Local demonstration data. Loaded only when the local profile is active.
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
