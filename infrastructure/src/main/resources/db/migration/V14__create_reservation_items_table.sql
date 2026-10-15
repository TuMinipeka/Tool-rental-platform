-- Based on V11__create_reservation_items_table.sql by developer/conde (ac149a1db7a95d3cc2bb4f313e063927fb7a0915).
-- Forward integration preserves the previously applied V1-V3 history.
CREATE TABLE IF NOT EXISTS reservation_items (
    reservation_id uuid NOT NULL REFERENCES reservations (reservation_id) ON DELETE CASCADE,
    equipment_unit_id uuid NOT NULL REFERENCES equipment_units (equipment_unit_id) ON DELETE RESTRICT,
    agreed_daily_rate numeric(19, 4) NOT NULL,
    PRIMARY KEY (reservation_id, equipment_unit_id),
    CONSTRAINT ck_reservation_items_rate_non_negative CHECK (agreed_daily_rate >= 0)
);

CREATE INDEX IF NOT EXISTS ix_reservation_items_equipment_unit_id
    ON reservation_items (equipment_unit_id, reservation_id);
