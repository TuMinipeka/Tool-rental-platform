CREATE TABLE tools (
    tool_id uuid PRIMARY KEY,
    supplier_id uuid NOT NULL REFERENCES suppliers (user_id) ON DELETE RESTRICT,
    category_id uuid NOT NULL REFERENCES categories (category_id) ON DELETE RESTRICT,
    name varchar(160) NOT NULL,
    description varchar(2000) NOT NULL,
    current_daily_rate numeric(19, 4) NOT NULL,
    rate_currency char(3) NOT NULL,
    publication_status varchar(20) NOT NULL,
    CONSTRAINT ck_tools_name_not_blank CHECK (btrim(name) <> ''),
    CONSTRAINT ck_tools_description_not_blank CHECK (btrim(description) <> ''),
    CONSTRAINT ck_tools_daily_rate_non_negative CHECK (current_daily_rate >= 0),
    CONSTRAINT ck_tools_currency_uppercase CHECK (rate_currency ~ '^[A-Z]{3}$'),
    CONSTRAINT ck_tools_publication_status CHECK (
        publication_status IN ('PUBLISHED', 'UNPUBLISHED')
    )
);

CREATE INDEX ix_tools_supplier_id ON tools (supplier_id);
CREATE INDEX ix_tools_category_id ON tools (category_id);
