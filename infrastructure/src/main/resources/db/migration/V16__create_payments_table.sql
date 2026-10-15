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
