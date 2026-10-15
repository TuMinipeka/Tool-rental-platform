CREATE TABLE roles (
    role_id smallint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code varchar(40) NOT NULL UNIQUE,
    description varchar(200) NOT NULL,
    CONSTRAINT ck_roles_code_not_blank CHECK (btrim(code) <> ''),
    CONSTRAINT ck_roles_description_not_blank CHECK (btrim(description) <> '')
);
