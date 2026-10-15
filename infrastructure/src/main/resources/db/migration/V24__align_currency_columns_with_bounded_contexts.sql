-- Based on V21__align_currency_columns_with_bounded_contexts.sql by developer/conde (ac149a1db7a95d3cc2bb4f313e063927fb7a0915).
-- Forward integration preserves the previously applied V1-V3 history.
SET LOCAL lock_timeout = '5s';

ALTER TABLE tools
    ALTER COLUMN rate_currency TYPE char(3);

ALTER TABLE reservations
    ALTER COLUMN currency TYPE char(3);
