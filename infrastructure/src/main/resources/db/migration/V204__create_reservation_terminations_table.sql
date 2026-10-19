CREATE TABLE reservation_terminations (
    reservation_termination_id uuid PRIMARY KEY,
    reservation_id uuid NOT NULL REFERENCES reservations (reservation_id) ON DELETE RESTRICT,
    termination_type varchar(30) NOT NULL,
    termination_status varchar(20) NOT NULL,
    requested_by uuid NOT NULL REFERENCES users (user_id) ON DELETE RESTRICT,
    requested_at timestamptz NOT NULL,
    resolved_by uuid REFERENCES users (user_id) ON DELETE RESTRICT,
    resolved_at timestamptz,
    reason varchar(500),
    fee_amount numeric(19, 4) NOT NULL,
    refund_amount numeric(19, 4) NOT NULL,
    outstanding_amount numeric(19, 4) NOT NULL,
    currency char(3) NOT NULL,
    idempotency_key varchar(200) NOT NULL,
    CONSTRAINT uq_reservation_terminations_idempotency UNIQUE (idempotency_key),
    CONSTRAINT ck_reservation_terminations_type CHECK (
        termination_type IN (
            'STANDARD', 'NO_SHOW', 'WITHDRAWAL', 'SUPPLIER_FAILURE', 'EXTERNAL_REVIEW'
        )
    ),
    CONSTRAINT ck_reservation_terminations_status CHECK (
        termination_status IN ('REQUESTED', 'APPLIED', 'APPROVED', 'REJECTED')
    ),
    CONSTRAINT ck_reservation_terminations_amounts CHECK (
        fee_amount >= 0 AND refund_amount >= 0 AND outstanding_amount >= 0
    ),
    CONSTRAINT ck_reservation_terminations_currency CHECK (currency ~ '^[A-Z]{3}$'),
    CONSTRAINT ck_reservation_terminations_resolution CHECK (
        (termination_status = 'REQUESTED' AND resolved_by IS NULL AND resolved_at IS NULL)
        OR (termination_status <> 'REQUESTED' AND resolved_at IS NOT NULL)
    )
);

CREATE UNIQUE INDEX ux_reservation_terminations_open
    ON reservation_terminations (reservation_id)
    WHERE termination_status IN ('REQUESTED', 'APPLIED', 'APPROVED');
