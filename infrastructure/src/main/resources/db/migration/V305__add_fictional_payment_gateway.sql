ALTER TABLE payments ADD COLUMN fictional_outcome varchar(20),
  ADD COLUMN fictional_refunded boolean NOT NULL DEFAULT false;
ALTER TABLE payments ADD CONSTRAINT ck_payments_fictional_outcome CHECK (
  (fictional_outcome IS NULL OR (processor='FICTIONAL_GATEWAY' AND fictional_outcome IN ('APPROVED','DECLINED')))
  AND (NOT fictional_refunded OR (processor='FICTIONAL_GATEWAY' AND fictional_outcome='APPROVED')));
ALTER TABLE payments DROP CONSTRAINT ck_payments_checkout;
ALTER TABLE payments ADD CONSTRAINT ck_payments_checkout CHECK (
  checkout_phase IS NULL OR (processor IN ('MERCADO_PAGO','FICTIONAL_GATEWAY') AND checkout_expires_at IS NOT NULL
    AND checkout_phase IN ('CREATING','READY','REVIEW','REFUND_PENDING','REFUNDED','DONE','REJECTED')));
DROP INDEX ux_payments_hosted_reservation;
CREATE UNIQUE INDEX ux_payments_hosted_reservation ON payments(reservation_id)
  WHERE processor IN ('MERCADO_PAGO','FICTIONAL_GATEWAY') AND payment_stage='FULL_PAYMENT';
