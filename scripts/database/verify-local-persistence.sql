-- Run on the application's connection, not on a different local PostgreSQL server.
SELECT current_database() AS database_name, current_user AS database_user,
       inet_server_addr() AS server_address, inet_server_port() AS internal_port;

SELECT 'users' AS entity, count(*) AS records FROM users
UNION ALL SELECT 'categories', count(*) FROM categories
UNION ALL SELECT 'tools', count(*) FROM tools
UNION ALL SELECT 'published_tools', count(*) FROM tools WHERE publication_status = 'PUBLISHED'
UNION ALL SELECT 'tool_images', count(*) FROM tool_images
UNION ALL SELECT 'equipment_units', count(*) FROM equipment_units;

SELECT t.tool_id, t.name, c.name AS category, t.service_city,
       t.current_daily_rate, t.rate_currency, t.publication_status,
       (SELECT count(*) FROM tool_images i WHERE i.tool_id = t.tool_id) AS images,
       (SELECT count(*) FROM equipment_units e WHERE e.tool_id = t.tool_id
        AND e.operational_status = 'OPERATIONAL') AS operational_units
FROM tools t JOIN categories c ON c.category_id = t.category_id
ORDER BY t.name;

SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;
