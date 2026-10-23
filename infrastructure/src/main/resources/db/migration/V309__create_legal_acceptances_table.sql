CREATE TABLE legal_acceptances (
    legal_acceptance_id uuid PRIMARY KEY,
    user_id uuid NOT NULL REFERENCES users(user_id),
    document_type varchar(50) NOT NULL,
    document_version varchar(40) NOT NULL,
    purpose varchar(80) NOT NULL,
    accepted_at timestamptz NOT NULL,
    CONSTRAINT ck_legal_acceptances_document_type CHECK (
        document_type IN ('TERMS_AND_CONDITIONS', 'PRIVACY_NOTICE')
    ),
    CONSTRAINT ck_legal_acceptances_purpose CHECK (purpose ~ '^[A-Z0-9_]+$'),
    CONSTRAINT uq_legal_acceptances_user_document
        UNIQUE (user_id, document_type, document_version, purpose)
);

CREATE INDEX ix_legal_acceptances_user_accepted_at
    ON legal_acceptances (user_id, accepted_at DESC);
