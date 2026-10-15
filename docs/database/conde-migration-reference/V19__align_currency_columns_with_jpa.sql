ALTER TABLE tools
    ALTER COLUMN rate_currency TYPE varchar(3);

ALTER TABLE reservations
    ALTER COLUMN currency TYPE varchar(3);
