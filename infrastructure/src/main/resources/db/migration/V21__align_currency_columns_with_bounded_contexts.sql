ALTER TABLE tools
    ALTER COLUMN rate_currency TYPE char(3);

ALTER TABLE reservations
    ALTER COLUMN currency TYPE char(3);
