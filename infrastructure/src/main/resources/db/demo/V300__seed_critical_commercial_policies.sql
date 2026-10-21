INSERT INTO delivery_rates (
    delivery_rate_id, rate_version, amount, currency, active, effective_at
) VALUES (
    '00000000-0000-0000-0000-000000000300', 1, 20000, 'COP', true, now()
) ON CONFLICT DO NOTHING;
