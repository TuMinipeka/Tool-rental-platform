-- Local demonstration data. Loaded only when the demo profile is explicitly active.
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
