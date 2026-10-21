-- Based on V3__create_user_roles_table.sql by developer/conde (ac149a1db7a95d3cc2bb4f313e063927fb7a0915).
-- Forward integration preserves the previously applied V1-V3 history.
CREATE TABLE IF NOT EXISTS user_roles (
    user_id uuid NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    role_id smallint NOT NULL REFERENCES roles (role_id) ON DELETE RESTRICT,
    PRIMARY KEY (user_id, role_id)
);
