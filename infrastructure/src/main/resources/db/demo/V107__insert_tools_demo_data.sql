-- Local demonstration data. Loaded only when the demo profile is explicitly active.
-- Daily COP rates are based on public Colombian rental listings consulted on 2026-10-15.
-- Ferreteria Donda: hammer drill 10,000; rotary hammer 20,000; circular saw 10,000; 42L compressor 50,000.
-- Diemec: two-bag concrete mixer 60,000. See docs/data-sources.md.
INSERT INTO tools (
    tool_id, supplier_id, category_id, name, description,
    current_daily_rate, rate_currency, publication_status
) VALUES (
    '20000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000001',
    '10000000-0000-0000-0000-000000000001',
    '800W Hammer Drill',
    'Corded 800W hammer drill for masonry and general-purpose drilling',
    10000.0000,
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
    '850W Rotary Hammer',
    'Corded 850W rotary hammer for drilling concrete and light chiselling',
    20000.0000,
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
    '7-1/4 Inch Circular Saw',
    'Portable 2-1/8 HP circular saw for timber and sheet materials',
    10000.0000,
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
    '42L Silent Air Compressor',
    'Portable silent air compressor with 42-liter tank and 2 HP motor',
    50000.0000,
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
    'Two-Bag Concrete Mixer',
    'Job-site concrete mixer with capacity for two cement bags per batch',
    60000.0000,
    'COP',
    'PUBLISHED'
);
