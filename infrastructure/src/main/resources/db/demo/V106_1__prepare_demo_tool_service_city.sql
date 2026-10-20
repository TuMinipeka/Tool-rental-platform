-- Supports enabling the demo profile after the main schema has already reached V306.
-- On a fresh installation the column does not exist yet, so V306 performs the backfill later.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'tools'
          AND column_name = 'service_city'
    ) THEN
        ALTER TABLE tools
            ALTER COLUMN service_city SET DEFAULT 'Bucaramanga';
    END IF;
END
$$;
