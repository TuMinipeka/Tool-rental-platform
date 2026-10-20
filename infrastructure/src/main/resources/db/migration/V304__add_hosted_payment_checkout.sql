ALTER TABLE payments
  ADD COLUMN checkout_expires_at timestamptz,
  ADD COLUMN checkout_preference_id varchar(200),
  ADD COLUMN checkout_url varchar(2000),
  ADD COLUMN provider_payment_id varchar(200),
  ADD COLUMN checkout_phase varchar(30),
  ADD COLUMN checkout_attempts integer NOT NULL DEFAULT 0,
  ADD COLUMN checkout_lease_until timestamptz,
  ADD COLUMN checkout_retry_at timestamptz;
ALTER TABLE payments DROP CONSTRAINT ck_payments_method;
ALTER TABLE payments ADD CONSTRAINT ck_payments_method CHECK
  (payment_method IN ('ADMINISTRATIVE','CREDIT_CARD','DEBIT_CARD','PAYPAL','HOSTED_CHECKOUT'));
ALTER TABLE payments DROP CONSTRAINT ck_payments_card_details;
ALTER TABLE payments ADD CONSTRAINT ck_payments_card_details CHECK (
  (payment_method IN ('CREDIT_CARD','DEBIT_CARD') AND card_network IN ('VISA','MASTERCARD')
    AND card_last_four ~ '^[0-9]{4}$' AND card_expiry_month BETWEEN 1 AND 12 AND card_expiry_year >= 2026)
  OR (payment_method IN ('PAYPAL','ADMINISTRATIVE','HOSTED_CHECKOUT') AND card_network IS NULL
    AND card_last_four IS NULL AND card_expiry_month IS NULL AND card_expiry_year IS NULL));
ALTER TABLE payments ADD CONSTRAINT ck_payments_checkout CHECK (
  checkout_phase IS NULL OR (processor = 'MERCADO_PAGO' AND checkout_expires_at IS NOT NULL
  AND checkout_phase IN ('CREATING','READY','REVIEW','REFUND_PENDING','REFUNDED','DONE','REJECTED')));
CREATE UNIQUE INDEX ux_payments_hosted_reservation ON payments(reservation_id)
  WHERE processor = 'MERCADO_PAGO' AND payment_stage = 'FULL_PAYMENT';
CREATE UNIQUE INDEX ux_payments_provider_payment ON payments(processor, provider_payment_id)
  WHERE provider_payment_id IS NOT NULL;
CREATE INDEX ix_payments_checkout_due ON payments(checkout_lease_until)
  WHERE checkout_phase IN ('CREATING','READY','REVIEW','REFUND_PENDING');
