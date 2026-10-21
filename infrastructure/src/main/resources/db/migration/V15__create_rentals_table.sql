-- Based on V12__create_rentals_table.sql by developer/conde (ac149a1db7a95d3cc2bb4f313e063927fb7a0915).
-- Forward integration preserves the previously applied V1-V3 history.
CREATE TABLE IF NOT EXISTS rentals (
    reservation_id uuid PRIMARY KEY REFERENCES reservations (reservation_id) ON DELETE RESTRICT,
    started_at timestamptz NOT NULL,
    rental_status varchar(20) NOT NULL,
    CONSTRAINT ck_rentals_status CHECK (
        rental_status IN ('ACTIVE', 'COMPLETED', 'CANCELLED')
    )
);
