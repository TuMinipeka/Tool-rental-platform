CREATE TABLE customers (
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
