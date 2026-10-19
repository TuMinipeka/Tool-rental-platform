CREATE TABLE delivery_rates (
    delivery_rate_id uuid PRIMARY KEY,
    rate_version integer NOT NULL,
    amount numeric(19, 4) NOT NULL,
    currency char(3) NOT NULL,
    active boolean NOT NULL,
    effective_at timestamptz NOT NULL,
    CONSTRAINT uq_delivery_rates_version UNIQUE (rate_version),
    CONSTRAINT ck_delivery_rates_version CHECK (rate_version > 0),
    CONSTRAINT ck_delivery_rates_amount CHECK (amount >= 0),
    CONSTRAINT ck_delivery_rates_currency CHECK (currency = 'COP')
);

CREATE UNIQUE INDEX ux_delivery_rates_active ON delivery_rates (active) WHERE active;
