ALTER TABLE reservations DROP CONSTRAINT IF EXISTS ck_reservations_status;
ALTER TABLE reservations ADD CONSTRAINT ck_reservations_status CHECK (
    reservation_status IN (
        'PENDING',
        'PENDING_PAYMENT',
        'CONFIRMED',
        'REJECTED',
        'CANCELLED',
        'PAYMENT_FAILED',
        'EXPIRED',
        'CANCELLATION_REVIEW',
        'NO_SHOW'
    )
);

ALTER TABLE reservations DROP CONSTRAINT IF EXISTS ck_reservations_confirmation;
ALTER TABLE reservations ADD CONSTRAINT ck_reservations_confirmation CHECK (
    (reservation_status IN ('CONFIRMED', 'CANCELLATION_REVIEW', 'NO_SHOW') AND confirmed_at IS NOT NULL)
    OR (reservation_status IN ('PENDING', 'PENDING_PAYMENT', 'REJECTED', 'PAYMENT_FAILED', 'EXPIRED') AND confirmed_at IS NULL)
    OR reservation_status = 'CANCELLED'
);

ALTER TABLE reservations ADD CONSTRAINT ck_reservations_payment_hold_expiration CHECK (
    reservation_status <> 'PENDING_PAYMENT' OR expires_at IS NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_reservations_active_holds
    ON reservations (expires_at, start_date, effective_return_date)
    WHERE reservation_status = 'PENDING_PAYMENT';
