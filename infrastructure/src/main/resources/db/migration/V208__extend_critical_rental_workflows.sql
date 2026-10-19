ALTER TABLE reservations
    ADD COLUMN scheduled_fulfillment_at timestamptz,
    ADD COLUMN fulfillment_method varchar(30),
    ADD COLUMN delivery_rate_id uuid REFERENCES delivery_rates (delivery_rate_id) ON DELETE RESTRICT,
    ADD COLUMN cancellation_policy_id uuid REFERENCES cancellation_policies (cancellation_policy_id) ON DELETE RESTRICT,
    ADD COLUMN delivery_fee numeric(19, 4),
    ADD COLUMN effective_return_date date,
    ADD COLUMN delivery_address_line varchar(300),
    ADD COLUMN delivery_city varchar(120),
    ADD COLUMN delivery_region varchar(120),
    ADD COLUMN delivery_country_code char(2);

UPDATE reservations
SET scheduled_fulfillment_at = (start_date::timestamp + time '08:00') AT TIME ZONE 'America/Bogota',
    fulfillment_method = 'CUSTOMER_PICKUP',
    cancellation_policy_id = '00000000-0000-0000-0000-000000000203',
    delivery_fee = 0,
    effective_return_date = return_date;

ALTER TABLE reservations
    ALTER COLUMN scheduled_fulfillment_at SET NOT NULL,
    ALTER COLUMN fulfillment_method SET NOT NULL,
    ALTER COLUMN cancellation_policy_id SET NOT NULL,
    ALTER COLUMN delivery_fee SET NOT NULL,
    ALTER COLUMN effective_return_date SET NOT NULL;

ALTER TABLE reservations DROP CONSTRAINT ck_reservations_status;
ALTER TABLE reservations ADD CONSTRAINT ck_reservations_status CHECK (
    reservation_status IN (
        'PENDING', 'CONFIRMED', 'REJECTED', 'CANCELLED', 'CANCELLATION_REVIEW', 'NO_SHOW'
    )
);
ALTER TABLE reservations DROP CONSTRAINT ck_reservations_confirmation;
ALTER TABLE reservations ADD CONSTRAINT ck_reservations_confirmation CHECK (
    (reservation_status IN ('CONFIRMED', 'CANCELLATION_REVIEW', 'NO_SHOW') AND confirmed_at IS NOT NULL)
    OR (reservation_status IN ('PENDING', 'REJECTED') AND confirmed_at IS NULL)
    OR reservation_status = 'CANCELLED'
);
ALTER TABLE reservations ADD CONSTRAINT ck_reservations_fulfillment CHECK (
    (fulfillment_method = 'CUSTOMER_PICKUP' AND delivery_rate_id IS NULL AND delivery_fee = 0
        AND delivery_address_line IS NULL AND delivery_city IS NULL
        AND delivery_region IS NULL AND delivery_country_code IS NULL)
    OR (fulfillment_method = 'SUPPLIER_DELIVERY' AND delivery_rate_id IS NOT NULL AND delivery_fee >= 0
        AND delivery_address_line IS NOT NULL AND delivery_city IS NOT NULL
        AND delivery_region IS NOT NULL AND delivery_country_code = 'CO')
);
ALTER TABLE reservations ADD CONSTRAINT ck_reservations_effective_period CHECK (
    effective_return_date >= return_date
);

ALTER TABLE unit_deliveries
    ADD COLUMN fulfillment_method varchar(30) NOT NULL DEFAULT 'SUPPLIER_DELIVERY',
    ADD COLUMN delivery_status varchar(20) NOT NULL DEFAULT 'SCHEDULED',
    ADD COLUMN failure_type varchar(30),
    ADD COLUMN failure_reason varchar(500),
    ADD COLUMN failed_at timestamptz;

-- Scheduling precedes rental activation; the original FK forced the opposite order.
ALTER TABLE unit_deliveries DROP CONSTRAINT fk_unit_deliveries_rental;

UPDATE unit_deliveries
SET delivery_status = CASE WHEN delivered_at IS NULL THEN 'SCHEDULED' ELSE 'COMPLETED' END;

ALTER TABLE unit_deliveries ADD CONSTRAINT ck_unit_deliveries_method CHECK (
    fulfillment_method IN ('CUSTOMER_PICKUP', 'SUPPLIER_DELIVERY')
);
ALTER TABLE unit_deliveries ADD CONSTRAINT ck_unit_deliveries_status CHECK (
    delivery_status IN ('SCHEDULED', 'COMPLETED', 'FAILED')
);
ALTER TABLE unit_deliveries ADD CONSTRAINT ck_unit_deliveries_failure CHECK (
    (delivery_status = 'FAILED' AND failed_at IS NOT NULL AND failure_type IS NOT NULL AND failure_reason IS NOT NULL)
    OR (delivery_status <> 'FAILED' AND failed_at IS NULL AND failure_type IS NULL AND failure_reason IS NULL)
);

ALTER TABLE unit_returns
    ADD COLUMN late_days integer NOT NULL DEFAULT 0,
    ADD COLUMN late_fee numeric(19, 4) NOT NULL DEFAULT 0,
    ADD COLUMN late_fee_currency char(3) NOT NULL DEFAULT 'COP';

ALTER TABLE unit_returns ADD CONSTRAINT ck_unit_returns_late_values CHECK (
    late_days >= 0 AND late_fee >= 0 AND late_fee_currency ~ '^[A-Z]{3}$'
);

ALTER TABLE payments
    ADD COLUMN adjustment_type varchar(30),
    ADD COLUMN adjustment_reference uuid;

ALTER TABLE payments ADD CONSTRAINT ck_payments_adjustment_reference CHECK (
    (payment_stage = 'ADJUSTMENT' AND adjustment_type IS NOT NULL AND adjustment_reference IS NOT NULL)
    OR (payment_stage <> 'ADJUSTMENT' AND adjustment_type IS NULL AND adjustment_reference IS NULL)
);
