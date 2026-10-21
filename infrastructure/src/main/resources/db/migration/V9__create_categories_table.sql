-- Based on V6__create_categories_table.sql by developer/conde (ac149a1db7a95d3cc2bb4f313e063927fb7a0915).
-- Forward integration preserves the previously applied V1-V3 history.
CREATE TABLE IF NOT EXISTS categories (
    category_id uuid PRIMARY KEY,
    name varchar(120) NOT NULL,
    description varchar(500),
    CONSTRAINT ck_categories_name_not_blank CHECK (btrim(name) <> '')
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_categories_name_lower ON categories (lower(name));
