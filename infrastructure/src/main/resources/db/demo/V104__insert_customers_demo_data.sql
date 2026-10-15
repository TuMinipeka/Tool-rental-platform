-- Local demonstration data. Loaded only when the demo profile is explicitly active.
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
