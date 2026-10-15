CREATE TABLE users (
    user_id uuid PRIMARY KEY,
    email varchar(320) NOT NULL,
    display_name varchar(200) NOT NULL,
    password_hash varchar(255) NOT NULL,
    account_status varchar(20) NOT NULL,
    registered_at timestamptz NOT NULL,
    CONSTRAINT ck_users_email_not_blank CHECK (btrim(email) <> ''),
    CONSTRAINT ck_users_display_name_not_blank CHECK (btrim(display_name) <> ''),
    CONSTRAINT ck_users_account_status CHECK (
        account_status IN ('ACTIVE', 'SUSPENDED', 'DISABLED')
    )
);

CREATE UNIQUE INDEX ux_users_email_lower ON users (lower(email));
