CREATE TABLE cancellation_policies (
    cancellation_policy_id uuid PRIMARY KEY,
    policy_version integer NOT NULL,
    cutoff_hours integer NOT NULL,
    late_cancellation_rate numeric(5, 4) NOT NULL,
    no_show_rate numeric(5, 4) NOT NULL,
    no_show_grace_minutes integer NOT NULL,
    active boolean NOT NULL,
    effective_at timestamptz NOT NULL,
    CONSTRAINT uq_cancellation_policies_version UNIQUE (policy_version),
    CONSTRAINT ck_cancellation_policies_values CHECK (
        policy_version > 0
        AND cutoff_hours >= 0
        AND late_cancellation_rate BETWEEN 0 AND 1
        AND no_show_rate BETWEEN 0 AND 1
        AND no_show_grace_minutes >= 0
    )
);

CREATE UNIQUE INDEX ux_cancellation_policies_active
    ON cancellation_policies (active) WHERE active;

INSERT INTO cancellation_policies (
    cancellation_policy_id, policy_version, cutoff_hours,
    late_cancellation_rate, no_show_rate, no_show_grace_minutes, active, effective_at
) VALUES (
    '00000000-0000-0000-0000-000000000203', 1, 48, 0.2500, 0.5000, 120, true, now()
);
