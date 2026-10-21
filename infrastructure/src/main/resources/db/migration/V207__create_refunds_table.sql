CREATE TABLE refunds (
    refund_id uuid PRIMARY KEY,
    payment_id uuid NOT NULL REFERENCES payments (payment_id) ON DELETE RESTRICT,
    reservation_termination_id uuid REFERENCES reservation_terminations (reservation_termination_id) ON DELETE RESTRICT,
    amount numeric(19, 4) NOT NULL,
    currency char(3) NOT NULL,
    refund_status varchar(20) NOT NULL,
    idempotency_key varchar(200) NOT NULL,
    recorded_at timestamptz NOT NULL,
    confirmed_at timestamptz,
    CONSTRAINT uq_refunds_idempotency UNIQUE (idempotency_key),
    CONSTRAINT ck_refunds_amount CHECK (amount > 0),
    CONSTRAINT ck_refunds_currency CHECK (currency ~ '^[A-Z]{3}$'),
    CONSTRAINT ck_refunds_status CHECK (
        refund_status IN ('PENDING', 'CONFIRMED', 'FAILED')
    ),
    CONSTRAINT ck_refunds_confirmation CHECK (
        (refund_status = 'CONFIRMED' AND confirmed_at IS NOT NULL)
        OR (refund_status <> 'CONFIRMED' AND confirmed_at IS NULL)
    )
);

CREATE INDEX ix_refunds_payment ON refunds (payment_id);
CREATE INDEX ix_refunds_termination ON refunds (reservation_termination_id);
