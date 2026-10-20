ALTER TABLE tools
    ADD COLUMN service_city varchar(120);

-- Existing records predate city-aware catalog filtering. Bucaramanga preserves the
-- previous local catalog default; later versioned data migrations may refine it.
UPDATE tools
SET service_city = 'Bucaramanga'
WHERE service_city IS NULL;

ALTER TABLE tools
    ALTER COLUMN service_city SET NOT NULL,
    ADD CONSTRAINT ck_tools_service_city_not_blank CHECK (btrim(service_city) <> '');

CREATE INDEX ix_tools_publication_city
    ON tools (publication_status, service_city);
