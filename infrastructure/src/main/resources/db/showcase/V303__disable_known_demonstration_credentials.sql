-- Loaded only by the isolated public academic showcase profile.
-- Provision private evaluator accounts explicitly before opening the service.
UPDATE users SET account_status='DISABLED';
UPDATE user_sessions SET session_status='REVOKED', revoked_at=now()
WHERE session_status IN ('ACTIVE','ROTATED');
UPDATE password_reset_tokens SET revoked_at=now() WHERE used_at IS NULL AND revoked_at IS NULL;
