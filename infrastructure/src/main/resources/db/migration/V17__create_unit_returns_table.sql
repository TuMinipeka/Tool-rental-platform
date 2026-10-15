-- Based on V14__create_unit_returns_table.sql by developer/conde (ac149a1db7a95d3cc2bb4f313e063927fb7a0915).
-- Forward integration preserves the previously applied V1-V3 history.
CREATE TABLE IF NOT EXISTS unit_returns (
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
