-- Based on V4__create_customers_table.sql by developer/conde (ac149a1db7a95d3cc2bb4f313e063927fb7a0915).
-- Forward integration preserves the previously applied V1-V3 history.
CREATE TABLE IF NOT EXISTS customers (
    user_id uuid PRIMARY KEY REFERENCES users (user_id) ON DELETE RESTRICT,
    person_type varchar(20) NOT NULL,
    document_type varchar(30) NOT NULL,
    document_number varchar(80) NOT NULL,
    billing_name varchar(200) NOT NULL,
    CONSTRAINT uq_customers_document UNIQUE (document_type, document_number),
    CONSTRAINT ck_customers_person_type CHECK (person_type IN ('PERSON', 'COMPANY')),
    CONSTRAINT ck_customers_document_not_blank CHECK (btrim(document_number) <> ''),
    CONSTRAINT ck_customers_billing_name_not_blank CHECK (btrim(billing_name) <> '')
);
