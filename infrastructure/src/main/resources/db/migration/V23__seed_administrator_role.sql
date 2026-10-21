-- Based on V20__seed_administrator_role.sql by developer/conde (ac149a1db7a95d3cc2bb4f313e063927fb7a0915).
-- Forward integration preserves the previously applied V1-V3 history.
INSERT INTO roles (code, description) VALUES ('ADMIN', 'Platform administrator') ON CONFLICT (code) DO NOTHING;
