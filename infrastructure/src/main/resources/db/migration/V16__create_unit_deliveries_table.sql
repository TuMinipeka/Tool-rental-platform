-- Based on V13__create_unit_deliveries_table.sql by developer/conde (ac149a1db7a95d3cc2bb4f313e063927fb7a0915).
-- Forward integration preserves the previously applied V1-V3 history.
CREATE TABLE IF NOT EXISTS unit_deliveries (
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
