CREATE TABLE user_roles (
    user_id uuid NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    role_id smallint NOT NULL REFERENCES roles (role_id) ON DELETE RESTRICT,
    PRIMARY KEY (user_id, role_id)
);
