CREATE TABLE invoice_items (
    invoice_id uuid NOT NULL REFERENCES invoices (invoice_id) ON DELETE CASCADE,
    line_number integer NOT NULL,
    issued_description varchar(500) NOT NULL,
    quantity numeric(19, 4) NOT NULL,
    issued_unit_price numeric(19, 4) NOT NULL,
    PRIMARY KEY (invoice_id, line_number),
    CONSTRAINT ck_invoice_items_line_number_positive CHECK (line_number > 0),
    CONSTRAINT ck_invoice_items_description_not_blank CHECK (btrim(issued_description) <> ''),
    CONSTRAINT ck_invoice_items_quantity_positive CHECK (quantity > 0),
    CONSTRAINT ck_invoice_items_unit_price_non_negative CHECK (issued_unit_price >= 0)
);
