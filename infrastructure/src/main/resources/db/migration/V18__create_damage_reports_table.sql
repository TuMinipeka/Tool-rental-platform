-- Based on V15__create_damage_reports_table.sql by developer/conde (ac149a1db7a95d3cc2bb4f313e063927fb7a0915).
-- Forward integration preserves the previously applied V1-V3 history.
CREATE TABLE IF NOT EXISTS damage_reports (
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

CREATE INDEX IF NOT EXISTS ix_damage_reports_return
    ON damage_reports (reservation_id, equipment_unit_id);
