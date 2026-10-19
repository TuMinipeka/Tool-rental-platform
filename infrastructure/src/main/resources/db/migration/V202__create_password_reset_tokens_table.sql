CREATE TABLE password_reset_tokens (
    password_reset_token_id uuid PRIMARY KEY,
    user_id uuid NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    token_hash char(64) NOT NULL,
    created_at timestamptz NOT NULL,
    expires_at timestamptz NOT NULL,
    used_at timestamptz,
    revoked_at timestamptz,
    CONSTRAINT uq_password_reset_tokens_hash UNIQUE (token_hash),
    CONSTRAINT ck_password_reset_tokens_time CHECK (created_at < expires_at),
    CONSTRAINT ck_password_reset_tokens_completion CHECK (
        used_at IS NULL OR revoked_at IS NULL
    )
);

CREATE INDEX ix_password_reset_tokens_user_active
    ON password_reset_tokens (user_id, expires_at)
    WHERE used_at IS NULL AND revoked_at IS NULL;
