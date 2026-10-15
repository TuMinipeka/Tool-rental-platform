CREATE TABLE suppliers (
    user_id uuid PRIMARY KEY REFERENCES users (user_id) ON DELETE RESTRICT,
    document_type varchar(30) NOT NULL,
    document_number varchar(80) NOT NULL,
    business_name varchar(200) NOT NULL,
    CONSTRAINT uq_suppliers_document UNIQUE (document_type, document_number),
    CONSTRAINT ck_suppliers_document_not_blank CHECK (btrim(document_number) <> ''),
    CONSTRAINT ck_suppliers_business_name_not_blank CHECK (btrim(business_name) <> '')
);
