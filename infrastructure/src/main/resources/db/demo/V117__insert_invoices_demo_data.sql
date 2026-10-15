-- Local demonstration data. Loaded only when the demo profile is explicitly active.
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
