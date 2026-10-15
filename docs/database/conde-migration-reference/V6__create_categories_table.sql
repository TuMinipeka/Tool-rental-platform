CREATE TABLE categories (
    category_id uuid PRIMARY KEY,
    name varchar(120) NOT NULL,
    description varchar(500),
    CONSTRAINT ck_categories_name_not_blank CHECK (btrim(name) <> '')
);

CREATE UNIQUE INDEX ux_categories_name_lower ON categories (lower(name));
