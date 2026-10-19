CREATE TABLE reservation_extensions (
    reservation_extension_id uuid PRIMARY KEY,
    reservation_id uuid NOT NULL REFERENCES reservations (reservation_id) ON DELETE RESTRICT,
    requested_by uuid NOT NULL REFERENCES users (user_id) ON DELETE RESTRICT,
    requested_return_date date NOT NULL,
    extension_status varchar(20) NOT NULL,
    requested_at timestamptz NOT NULL,
    resolved_by uuid REFERENCES users (user_id) ON DELETE RESTRICT,
    resolved_at timestamptz,
    additional_amount numeric(19, 4) NOT NULL,
    currency char(3) NOT NULL,
    idempotency_key varchar(200) NOT NULL,
    CONSTRAINT uq_reservation_extensions_idempotency UNIQUE (idempotency_key),
    CONSTRAINT ck_reservation_extensions_status CHECK (
        extension_status IN ('REQUESTED', 'APPROVED', 'REJECTED')
    ),
    CONSTRAINT ck_reservation_extensions_amount CHECK (additional_amount >= 0),
    CONSTRAINT ck_reservation_extensions_currency CHECK (currency ~ '^[A-Z]{3}$'),
    CONSTRAINT ck_reservation_extensions_resolution CHECK (
        (extension_status = 'REQUESTED' AND resolved_by IS NULL AND resolved_at IS NULL)
        OR (extension_status <> 'REQUESTED' AND resolved_by IS NOT NULL AND resolved_at IS NOT NULL)
    )
);

CREATE UNIQUE INDEX ux_reservation_extensions_open
    ON reservation_extensions (reservation_id) WHERE extension_status = 'REQUESTED';
