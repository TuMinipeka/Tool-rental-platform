ALTER TABLE payments DROP CONSTRAINT IF EXISTS ck_payments_stage;
ALTER TABLE payments ADD CONSTRAINT ck_payments_stage CHECK (
    payment_stage IN ('ADMINISTRATIVE', 'FIRST_HALF', 'FINAL_HALF', 'FULL_PAYMENT', 'ADJUSTMENT')
);

DROP INDEX IF EXISTS ux_payments_confirmed_stage;
CREATE UNIQUE INDEX ux_payments_confirmed_stage
    ON payments (reservation_id, payment_stage)
    WHERE payment_status = 'CONFIRMED'
      AND payment_stage IN ('FIRST_HALF', 'FINAL_HALF', 'FULL_PAYMENT');

ALTER TABLE invoices DROP CONSTRAINT IF EXISTS invoices_reservation_id_fkey;
ALTER TABLE invoices ADD CONSTRAINT invoices_reservation_id_fkey
    FOREIGN KEY (reservation_id) REFERENCES reservations (reservation_id) ON DELETE RESTRICT;
