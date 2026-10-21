-- Run this script from an administrative PostgreSQL connection in DBeaver.
-- This changes concurrent connection capacity, not the number of application users.
-- The change applies to new connections without restarting PostgreSQL.
-- Keep application pool sizes within the role and server connection budgets.

-- Inspect the current per-role limit and open sessions.
SELECT role.rolname,
       role.rolconnlimit AS connection_limit,
       (SELECT count(*)
          FROM pg_stat_activity activity
         WHERE activity.usename = role.rolname) AS open_connections
  FROM pg_roles role
 WHERE role.rolname = 'filip';

-- Raise this role's concurrent connection limit to 20.
ALTER ROLE "filip" CONNECTION LIMIT 20;

-- Verify the change.
SELECT rolname, rolconnlimit AS connection_limit
  FROM pg_roles
 WHERE rolname = 'filip';

-- Server-wide capacity is a separate setting; this script does not modify it.
SHOW max_connections;
