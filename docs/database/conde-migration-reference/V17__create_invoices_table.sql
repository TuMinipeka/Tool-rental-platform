CREATE TABLE invoices (
    invoice_id uuid PRIMARY KEY,
    reservation_id uuid NOT NULL UNIQUE REFERENCES rentals (reservation_id) ON DELETE RESTRICT,
    number varchar(80) NOT NULL UNIQUE,
    issued_at timestamptz NOT NULL,
    invoice_status varchar(20) NOT NULL,
    issued_supplier_name varchar(200) NOT NULL,
    issued_supplier_document varchar(120) NOT NULL,
    issued_customer_name varchar(200) NOT NULL,
    issued_customer_document varchar(120) NOT NULL,
    CONSTRAINT ck_invoices_number_not_blank CHECK (btrim(number) <> ''),
    CONSTRAINT ck_invoices_status CHECK (
        invoice_status IN ('ISSUED', 'VOIDED', 'PAID')
    )
);
