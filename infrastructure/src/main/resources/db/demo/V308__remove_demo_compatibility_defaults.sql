-- Application writes must provide required values after the demonstration data is loaded.
ALTER TABLE tools
    ALTER COLUMN service_city DROP DEFAULT;

ALTER TABLE reservations
    ALTER COLUMN scheduled_fulfillment_at DROP DEFAULT,
    ALTER COLUMN fulfillment_method DROP DEFAULT,
    ALTER COLUMN cancellation_policy_id DROP DEFAULT,
    ALTER COLUMN delivery_fee DROP DEFAULT,
    ALTER COLUMN effective_return_date DROP DEFAULT;

ALTER TABLE payments
    ALTER COLUMN payment_method DROP DEFAULT,
    ALTER COLUMN payment_stage DROP DEFAULT,
    ALTER COLUMN idempotency_key DROP DEFAULT;
