-- Based on V5__create_suppliers_table.sql by developer/conde (ac149a1db7a95d3cc2bb4f313e063927fb7a0915).
-- Forward integration preserves the previously applied V1-V3 history.
CREATE TABLE IF NOT EXISTS suppliers (
    user_id uuid PRIMARY KEY REFERENCES users (user_id) ON DELETE RESTRICT,
    document_type varchar(30) NOT NULL,
    document_number varchar(80) NOT NULL,
    business_name varchar(200) NOT NULL,
    CONSTRAINT uq_suppliers_document UNIQUE (document_type, document_number),
    CONSTRAINT ck_suppliers_document_not_blank CHECK (btrim(document_number) <> ''),
    CONSTRAINT ck_suppliers_business_name_not_blank CHECK (btrim(business_name) <> '')
);
