-- Supports enabling the demo profile after the payment detail columns already exist.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'payments'
          AND column_name = 'payment_method'
    ) THEN
        ALTER TABLE payments
            ALTER COLUMN payment_method SET DEFAULT 'ADMINISTRATIVE',
            ALTER COLUMN payment_stage SET DEFAULT 'ADMINISTRATIVE',
            ALTER COLUMN idempotency_key SET DEFAULT gen_random_uuid()::text;
    END IF;
END
$$;
