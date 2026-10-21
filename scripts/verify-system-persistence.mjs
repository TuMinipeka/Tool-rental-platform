import { execFileSync } from 'node:child_process'
import { mkdirSync, writeFileSync } from 'node:fs'

const compose = ['compose', '--env-file', '.env.validation', '-f', 'compose.validation.yml']
const run = args => execFileSync('docker', [...compose, ...args], { encoding: 'utf8' }).trim()
const url = run(['exec', '-T', 'backend', 'printenv', 'DB_URL'])
const database = new URL(url.replace('jdbc:', '')).pathname.slice(1)
const sql = `WITH fixture AS (
  SELECT tool_id, name FROM tools WHERE name LIKE 'INSO Drill % Updated' ORDER BY name DESC LIMIT 1
), units AS (
  SELECT equipment_unit_id FROM equipment_units WHERE tool_id IN (SELECT tool_id FROM fixture)
), bookings AS (
  SELECT r.reservation_id, r.reservation_status FROM reservations r
  JOIN reservation_items i USING (reservation_id) WHERE i.equipment_unit_id IN (SELECT equipment_unit_id FROM units)
)
SELECT json_build_object(
 'fixture', (SELECT name FROM fixture),
 'tools', (SELECT count(*) FROM fixture),
 'equipmentUnits', (SELECT count(*) FROM units),
 'reservations', (SELECT json_agg(reservation_status ORDER BY reservation_status) FROM bookings),
 'payments', (SELECT json_agg(payment_status ORDER BY payment_status) FROM payments WHERE reservation_id IN (SELECT reservation_id FROM bookings)),
 'invoices', (SELECT count(*) FROM invoices WHERE reservation_id IN (SELECT reservation_id FROM bookings)),
 'rentals', (SELECT json_agg(rental_status) FROM rentals WHERE reservation_id IN (SELECT reservation_id FROM bookings)),
 'deliveries', (SELECT count(*) FROM unit_deliveries WHERE delivered_at IS NOT NULL AND reservation_id IN (SELECT reservation_id FROM bookings)),
 'returns', (SELECT count(*) FROM unit_returns WHERE reservation_id IN (SELECT reservation_id FROM bookings))
);`
const data = JSON.parse(run(['exec', '-T', 'postgres', 'psql', '-U', 'mvp_validation', '-d', database, '-At', '-c', sql]))
const passed = data.tools === 1 && data.equipmentUnits === 1 && data.invoices === 1 && data.deliveries === 1 && data.returns === 1
  && JSON.stringify(data.reservations) === '["CONFIRMED","PAYMENT_FAILED"]'
  && JSON.stringify(data.payments) === '["CONFIRMED","FAILED"]'
  && JSON.stringify(data.rentals) === '["COMPLETED"]'
mkdirSync('docs/evidence/system-tests', { recursive: true })
writeFileSync('docs/evidence/system-tests/persistence.json', JSON.stringify({ testedAt: new Date().toISOString(), database, passed, ...data }, null, 2) + '\n')
console.log(JSON.stringify({ passed, ...data }))
if (!passed) process.exitCode = 1
