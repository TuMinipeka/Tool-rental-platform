-- Based on V2__create_roles_table.sql by developer/conde (ac149a1db7a95d3cc2bb4f313e063927fb7a0915).
-- Forward integration preserves the previously applied V1-V3 history.
CREATE TABLE IF NOT EXISTS roles (
    role_id smallint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code varchar(40) NOT NULL UNIQUE,
    description varchar(200) NOT NULL,
    CONSTRAINT ck_roles_code_not_blank CHECK (btrim(code) <> ''),
    CONSTRAINT ck_roles_description_not_blank CHECK (btrim(description) <> '')
);
