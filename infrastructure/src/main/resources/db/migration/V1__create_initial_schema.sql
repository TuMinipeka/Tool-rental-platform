CREATE TABLE users (
    user_id uuid PRIMARY KEY,
    email varchar(320) NOT NULL,
    display_name varchar(200) NOT NULL,
    password_hash varchar(255) NOT NULL,
    account_status varchar(20) NOT NULL,
    registered_at timestamptz NOT NULL,
    CONSTRAINT ck_users_email_not_blank CHECK (btrim(email) <> ''),
    CONSTRAINT ck_users_display_name_not_blank CHECK (btrim(display_name) <> ''),
    CONSTRAINT ck_users_account_status CHECK (
        account_status IN ('ACTIVE', 'SUSPENDED', 'DISABLED')
    )
);

CREATE UNIQUE INDEX ux_users_email_lower ON users (lower(email));

CREATE TABLE roles (
    role_id smallint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code varchar(40) NOT NULL UNIQUE,
    description varchar(200) NOT NULL,
    CONSTRAINT ck_roles_code_not_blank CHECK (btrim(code) <> ''),
    CONSTRAINT ck_roles_description_not_blank CHECK (btrim(description) <> '')
);

CREATE TABLE user_roles (
    user_id uuid NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    role_id smallint NOT NULL REFERENCES roles (role_id) ON DELETE RESTRICT,
    PRIMARY KEY (user_id, role_id)
);

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

CREATE TABLE suppliers (
    user_id uuid PRIMARY KEY REFERENCES users (user_id) ON DELETE RESTRICT,
    document_type varchar(30) NOT NULL,
    document_number varchar(80) NOT NULL,
    business_name varchar(200) NOT NULL,
    CONSTRAINT uq_suppliers_document UNIQUE (document_type, document_number),
    CONSTRAINT ck_suppliers_document_not_blank CHECK (btrim(document_number) <> ''),
    CONSTRAINT ck_suppliers_business_name_not_blank CHECK (btrim(business_name) <> '')
);

CREATE TABLE categories (
    category_id uuid PRIMARY KEY,
    name varchar(120) NOT NULL,
    description varchar(500),
    CONSTRAINT ck_categories_name_not_blank CHECK (btrim(name) <> '')
);

CREATE UNIQUE INDEX ux_categories_name_lower ON categories (lower(name));

CREATE TABLE tools (
    tool_id uuid PRIMARY KEY,
    supplier_id uuid NOT NULL REFERENCES suppliers (user_id) ON DELETE RESTRICT,
    category_id uuid NOT NULL REFERENCES categories (category_id) ON DELETE RESTRICT,
    name varchar(160) NOT NULL,
    description varchar(2000) NOT NULL,
    current_daily_rate numeric(19, 4) NOT NULL,
    rate_currency char(3) NOT NULL,
    publication_status varchar(20) NOT NULL,
    CONSTRAINT ck_tools_name_not_blank CHECK (btrim(name) <> ''),
    CONSTRAINT ck_tools_description_not_blank CHECK (btrim(description) <> ''),
    CONSTRAINT ck_tools_daily_rate_non_negative CHECK (current_daily_rate >= 0),
    CONSTRAINT ck_tools_currency_uppercase CHECK (rate_currency ~ '^[A-Z]{3}$'),
    CONSTRAINT ck_tools_publication_status CHECK (
        publication_status IN ('PUBLISHED', 'UNPUBLISHED')
    )
);

CREATE INDEX ix_tools_supplier_id ON tools (supplier_id);
CREATE INDEX ix_tools_category_id ON tools (category_id);

CREATE TABLE tool_images (
    tool_id uuid NOT NULL REFERENCES tools (tool_id) ON DELETE CASCADE,
    image_url varchar(2048) NOT NULL,
    PRIMARY KEY (tool_id, image_url),
    CONSTRAINT ck_tool_images_url_not_blank CHECK (btrim(image_url) <> '')
);

CREATE TABLE equipment_units (
    equipment_unit_id uuid PRIMARY KEY,
    tool_id uuid NOT NULL REFERENCES tools (tool_id) ON DELETE RESTRICT,
    inventory_code varchar(120) NOT NULL,
    serial_number varchar(160),
    current_condition varchar(40) NOT NULL DEFAULT 'GOOD',
    operational_status varchar(20) NOT NULL,
    CONSTRAINT ck_equipment_units_inventory_code_not_blank CHECK (btrim(inventory_code) <> ''),
    CONSTRAINT ck_equipment_units_current_condition_not_blank CHECK (
        btrim(current_condition) <> ''
    ),
    CONSTRAINT ck_equipment_units_operational_status CHECK (
        operational_status IN ('OPERATIONAL', 'MAINTENANCE', 'RETIRED')
    )
);

CREATE INDEX ix_equipment_units_tool_id ON equipment_units (tool_id);
CREATE INDEX ix_equipment_units_inventory_code_lower ON equipment_units (lower(inventory_code));

CREATE FUNCTION enforce_supplier_inventory_code_uniqueness()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    owning_supplier_id uuid;
BEGIN
    SELECT supplier_id
      INTO STRICT owning_supplier_id
      FROM tools
     WHERE tool_id = NEW.tool_id;

    PERFORM pg_advisory_xact_lock(hashtextextended(owning_supplier_id::text, 0));

    IF EXISTS (
        SELECT 1
          FROM equipment_units existing_unit
          JOIN tools existing_tool ON existing_tool.tool_id = existing_unit.tool_id
         WHERE existing_tool.supplier_id = owning_supplier_id
           AND lower(existing_unit.inventory_code) = lower(NEW.inventory_code)
           AND existing_unit.equipment_unit_id <> NEW.equipment_unit_id
    ) THEN
        RAISE EXCEPTION 'Inventory code must be unique within a supplier'
            USING ERRCODE = '23505',
                  CONSTRAINT = 'uq_equipment_units_supplier_inventory_code';
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_equipment_units_supplier_inventory_code
BEFORE INSERT OR UPDATE OF tool_id, inventory_code ON equipment_units
FOR EACH ROW
EXECUTE FUNCTION enforce_supplier_inventory_code_uniqueness();

CREATE TABLE reservations (
    reservation_id uuid PRIMARY KEY,
    customer_id uuid NOT NULL REFERENCES customers (user_id) ON DELETE RESTRICT,
    created_at timestamptz NOT NULL,
    start_date date NOT NULL,
    return_date date NOT NULL,
    expires_at timestamptz,
    reservation_status varchar(20) NOT NULL,
    currency char(3) NOT NULL,
    confirmed_at timestamptz,
    CONSTRAINT ck_reservations_period CHECK (start_date <= return_date),
    CONSTRAINT ck_reservations_status CHECK (
        reservation_status IN ('PENDING', 'CONFIRMED', 'REJECTED', 'CANCELLED')
    ),
    CONSTRAINT ck_reservations_currency_uppercase CHECK (currency ~ '^[A-Z]{3}$'),
    CONSTRAINT ck_reservations_confirmation CHECK (
        (reservation_status = 'CONFIRMED' AND confirmed_at IS NOT NULL)
        OR (reservation_status IN ('PENDING', 'REJECTED') AND confirmed_at IS NULL)
        OR reservation_status = 'CANCELLED'
    )
);

CREATE INDEX ix_reservations_customer_id ON reservations (customer_id);
CREATE INDEX ix_reservations_status_period
    ON reservations (reservation_status, start_date, return_date);

CREATE TABLE reservation_items (
    reservation_id uuid NOT NULL REFERENCES reservations (reservation_id) ON DELETE CASCADE,
    equipment_unit_id uuid NOT NULL REFERENCES equipment_units (equipment_unit_id) ON DELETE RESTRICT,
    agreed_daily_rate numeric(19, 4) NOT NULL,
    PRIMARY KEY (reservation_id, equipment_unit_id),
    CONSTRAINT ck_reservation_items_rate_non_negative CHECK (agreed_daily_rate >= 0)
);

CREATE INDEX ix_reservation_items_equipment_unit_id
    ON reservation_items (equipment_unit_id, reservation_id);

CREATE TABLE rentals (
    reservation_id uuid PRIMARY KEY REFERENCES reservations (reservation_id) ON DELETE RESTRICT,
    started_at timestamptz NOT NULL,
    rental_status varchar(20) NOT NULL,
    CONSTRAINT ck_rentals_status CHECK (
        rental_status IN ('ACTIVE', 'COMPLETED', 'CANCELLED')
    )
);

CREATE TABLE unit_deliveries (
    reservation_id uuid NOT NULL,
    equipment_unit_id uuid NOT NULL,
    scheduled_at timestamptz NOT NULL,
    delivered_at timestamptz,
    outbound_condition varchar(500),
    address_line varchar(300) NOT NULL,
    city varchar(120) NOT NULL,
    region varchar(120) NOT NULL,
    country varchar(120) NOT NULL,
    PRIMARY KEY (reservation_id, equipment_unit_id),
    CONSTRAINT fk_unit_deliveries_item
        FOREIGN KEY (reservation_id, equipment_unit_id)
        REFERENCES reservation_items (reservation_id, equipment_unit_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_unit_deliveries_rental
        FOREIGN KEY (reservation_id)
        REFERENCES rentals (reservation_id)
        ON DELETE RESTRICT,
    CONSTRAINT ck_unit_deliveries_address_not_blank CHECK (btrim(address_line) <> ''),
    CONSTRAINT ck_unit_deliveries_city_not_blank CHECK (btrim(city) <> ''),
    CONSTRAINT ck_unit_deliveries_region_not_blank CHECK (btrim(region) <> ''),
    CONSTRAINT ck_unit_deliveries_country_not_blank CHECK (btrim(country) <> '')
);

CREATE TABLE unit_returns (
    reservation_id uuid NOT NULL,
    equipment_unit_id uuid NOT NULL,
    received_at timestamptz NOT NULL,
    inbound_condition varchar(500) NOT NULL,
    notes varchar(2000),
    PRIMARY KEY (reservation_id, equipment_unit_id),
    CONSTRAINT fk_unit_returns_delivery
        FOREIGN KEY (reservation_id, equipment_unit_id)
        REFERENCES unit_deliveries (reservation_id, equipment_unit_id)
        ON DELETE RESTRICT,
    CONSTRAINT ck_unit_returns_condition_not_blank CHECK (btrim(inbound_condition) <> '')
);

CREATE TABLE damage_reports (
    damage_report_id uuid PRIMARY KEY,
    reservation_id uuid NOT NULL,
    equipment_unit_id uuid NOT NULL,
    reported_at timestamptz NOT NULL,
    description varchar(2000) NOT NULL,
    report_status varchar(20) NOT NULL,
    assessed_cost numeric(19, 4),
    CONSTRAINT fk_damage_reports_return
        FOREIGN KEY (reservation_id, equipment_unit_id)
        REFERENCES unit_returns (reservation_id, equipment_unit_id)
        ON DELETE RESTRICT,
    CONSTRAINT ck_damage_reports_description_not_blank CHECK (btrim(description) <> ''),
    CONSTRAINT ck_damage_reports_status CHECK (
        report_status IN ('OPEN', 'ASSESSED', 'CLOSED')
    ),
    CONSTRAINT ck_damage_reports_assessed_cost CHECK (
        assessed_cost IS NULL OR assessed_cost >= 0
    )
);

CREATE INDEX ix_damage_reports_return
    ON damage_reports (reservation_id, equipment_unit_id);

CREATE TABLE payments (
    payment_id uuid PRIMARY KEY,
    reservation_id uuid NOT NULL REFERENCES reservations (reservation_id) ON DELETE RESTRICT,
    processor varchar(80) NOT NULL,
    external_reference varchar(200) NOT NULL,
    amount numeric(19, 4) NOT NULL,
    payment_status varchar(20) NOT NULL,
    recorded_at timestamptz NOT NULL,
    confirmed_at timestamptz,
    CONSTRAINT uq_payments_processor_reference UNIQUE (processor, external_reference),
    CONSTRAINT ck_payments_processor_not_blank CHECK (btrim(processor) <> ''),
    CONSTRAINT ck_payments_reference_not_blank CHECK (btrim(external_reference) <> ''),
    CONSTRAINT ck_payments_amount_positive CHECK (amount > 0),
    CONSTRAINT ck_payments_status CHECK (
        payment_status IN ('PENDING', 'CONFIRMED', 'FAILED', 'REFUNDED')
    )
);

CREATE INDEX ix_payments_reservation_id ON payments (reservation_id);

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
