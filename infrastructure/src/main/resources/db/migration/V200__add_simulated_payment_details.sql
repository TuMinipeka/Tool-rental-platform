ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS payment_method varchar(30),
    ADD COLUMN IF NOT EXISTS payment_stage varchar(30),
    ADD COLUMN IF NOT EXISTS idempotency_key varchar(200),
    ADD COLUMN IF NOT EXISTS card_network varchar(20),
    ADD COLUMN IF NOT EXISTS card_last_four varchar(4),
    ADD COLUMN IF NOT EXISTS card_expiry_month smallint,
    ADD COLUMN IF NOT EXISTS card_expiry_year smallint;

UPDATE payments
SET payment_method = COALESCE(payment_method, 'ADMINISTRATIVE'),
    payment_stage = COALESCE(payment_stage, 'ADMINISTRATIVE'),
    idempotency_key = COALESCE(idempotency_key, 'legacy:' || payment_id::text);

ALTER TABLE payments
    ALTER COLUMN payment_method SET NOT NULL,
    ALTER COLUMN payment_stage SET NOT NULL,
    ALTER COLUMN idempotency_key SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_payments_idempotency_key
    ON payments (idempotency_key);

CREATE UNIQUE INDEX IF NOT EXISTS ux_payments_confirmed_stage
    ON payments (reservation_id, payment_stage)
    WHERE payment_status = 'CONFIRMED'
      AND payment_stage IN ('FIRST_HALF', 'FINAL_HALF');

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_payments_method'
    ) THEN
        ALTER TABLE payments ADD CONSTRAINT ck_payments_method CHECK (
            payment_method IN ('ADMINISTRATIVE', 'CREDIT_CARD', 'DEBIT_CARD', 'PAYPAL')
        );
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_payments_stage'
    ) THEN
        ALTER TABLE payments ADD CONSTRAINT ck_payments_stage CHECK (
            payment_stage IN ('ADMINISTRATIVE', 'FIRST_HALF', 'FINAL_HALF', 'ADJUSTMENT')
        );
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_payments_card_details'
    ) THEN
        ALTER TABLE payments ADD CONSTRAINT ck_payments_card_details CHECK (
            (
                payment_method IN ('CREDIT_CARD', 'DEBIT_CARD')
                AND card_network IN ('VISA', 'MASTERCARD')
                AND card_last_four ~ '^[0-9]{4}$'
                AND card_expiry_month BETWEEN 1 AND 12
                AND card_expiry_year >= 2026
            ) OR (
                payment_method IN ('PAYPAL', 'ADMINISTRATIVE')
                AND card_network IS NULL
                AND card_last_four IS NULL
                AND card_expiry_month IS NULL
                AND card_expiry_year IS NULL
            )
        );
    END IF;
END $$;
