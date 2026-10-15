CREATE TABLE reservation_items (
    reservation_id uuid NOT NULL REFERENCES reservations (reservation_id) ON DELETE CASCADE,
    equipment_unit_id uuid NOT NULL REFERENCES equipment_units (equipment_unit_id) ON DELETE RESTRICT,
    agreed_daily_rate numeric(19, 4) NOT NULL,
    PRIMARY KEY (reservation_id, equipment_unit_id),
    CONSTRAINT ck_reservation_items_rate_non_negative CHECK (agreed_daily_rate >= 0)
);

CREATE INDEX ix_reservation_items_equipment_unit_id
    ON reservation_items (equipment_unit_id, reservation_id);
