CREATE TABLE rentals (
    reservation_id uuid PRIMARY KEY REFERENCES reservations (reservation_id) ON DELETE RESTRICT,
    started_at timestamptz NOT NULL,
    rental_status varchar(20) NOT NULL,
    CONSTRAINT ck_rentals_status CHECK (
        rental_status IN ('ACTIVE', 'COMPLETED', 'CANCELLED')
    )
);
