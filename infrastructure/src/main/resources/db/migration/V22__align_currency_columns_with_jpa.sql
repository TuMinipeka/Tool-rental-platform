-- Based on V19__align_currency_columns_with_jpa.sql by developer/conde (ac149a1db7a95d3cc2bb4f313e063927fb7a0915).
-- Forward integration preserves the previously applied V1-V3 history.
SET LOCAL lock_timeout = '5s';

ALTER TABLE tools
    ALTER COLUMN rate_currency TYPE varchar(3);

ALTER TABLE reservations
    ALTER COLUMN currency TYPE varchar(3);
