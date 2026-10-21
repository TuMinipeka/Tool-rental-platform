ALTER TABLE payments DROP CONSTRAINT ck_payments_method;
ALTER TABLE payments ADD CONSTRAINT ck_payments_method CHECK
  (payment_method IN ('ADMINISTRATIVE','CREDIT_CARD','DEBIT_CARD','PAYPAL','PSE','MERCADO_PAGO','HOSTED_CHECKOUT'));

ALTER TABLE payments DROP CONSTRAINT ck_payments_card_details;
ALTER TABLE payments ADD CONSTRAINT ck_payments_card_details CHECK (
  (payment_method IN ('CREDIT_CARD','DEBIT_CARD') AND card_network IN ('VISA','MASTERCARD')
    AND card_last_four ~ '^[0-9]{4}$' AND card_expiry_month BETWEEN 1 AND 12 AND card_expiry_year >= 2026)
  OR (payment_method IN ('PAYPAL','PSE','MERCADO_PAGO','ADMINISTRATIVE','HOSTED_CHECKOUT')
    AND card_network IS NULL AND card_last_four IS NULL AND card_expiry_month IS NULL
    AND card_expiry_year IS NULL));
