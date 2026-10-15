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
