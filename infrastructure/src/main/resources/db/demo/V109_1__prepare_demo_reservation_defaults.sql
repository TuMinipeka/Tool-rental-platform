-- Supports enabling the demo profile after the reservation workflow columns already exist.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'reservations'
          AND column_name = 'scheduled_fulfillment_at'
    ) THEN
        ALTER TABLE reservations
            ALTER COLUMN scheduled_fulfillment_at SET DEFAULT '2026-01-01T13:00:00Z',
            ALTER COLUMN fulfillment_method SET DEFAULT 'CUSTOMER_PICKUP',
            ALTER COLUMN cancellation_policy_id SET DEFAULT '00000000-0000-0000-0000-000000000203',
            ALTER COLUMN delivery_fee SET DEFAULT 0,
            ALTER COLUMN effective_return_date SET DEFAULT '9999-12-31';
    END IF;
END
$$;
