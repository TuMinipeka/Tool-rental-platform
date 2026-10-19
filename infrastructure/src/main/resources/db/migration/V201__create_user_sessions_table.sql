CREATE TABLE user_sessions (
    session_id uuid PRIMARY KEY,
    family_id uuid NOT NULL,
    user_id uuid NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    refresh_token_hash char(64) NOT NULL,
    session_status varchar(20) NOT NULL,
    created_at timestamptz NOT NULL,
    expires_at timestamptz NOT NULL,
    rotated_at timestamptz,
    revoked_at timestamptz,
    replaced_by_session_id uuid REFERENCES user_sessions (session_id) ON DELETE SET NULL,
    client_label varchar(300),
    CONSTRAINT uq_user_sessions_refresh_hash UNIQUE (refresh_token_hash),
    CONSTRAINT ck_user_sessions_status CHECK (
        session_status IN ('ACTIVE', 'ROTATED', 'REVOKED', 'COMPROMISED', 'EXPIRED')
    ),
    CONSTRAINT ck_user_sessions_time CHECK (created_at < expires_at),
    CONSTRAINT ck_user_sessions_transition_time CHECK (
        (session_status = 'ACTIVE' AND rotated_at IS NULL AND revoked_at IS NULL)
        OR (session_status = 'ROTATED' AND rotated_at IS NOT NULL AND revoked_at IS NULL)
        OR (session_status IN ('REVOKED', 'COMPROMISED') AND revoked_at IS NOT NULL)
        OR session_status = 'EXPIRED'
    )
);

CREATE INDEX ix_user_sessions_user_status
    ON user_sessions (user_id, session_status, expires_at);
CREATE INDEX ix_user_sessions_family
    ON user_sessions (family_id, session_status);
