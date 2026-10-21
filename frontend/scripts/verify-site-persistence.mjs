import { execFileSync } from 'node:child_process'
import { createHash } from 'node:crypto'
import { readFileSync, writeFileSync, mkdirSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { sourceFingerprint } from '../../scripts/source-fingerprint.mjs'

const root = fileURLToPath(new URL('../../', import.meta.url))
const frontend = root + 'frontend/'
const cli = frontend + 'node_modules/playwright/cli.js'
const output = root + 'docs/evidence/provider-persistence/'
const compose = ['compose', '--env-file', '.env.validation', '-f', 'compose.validation.yml']
const privateSettings = Object.fromEntries(readFileSync(root + '.env.validation', 'utf8').split(/\r?\n/).filter(Boolean).map(line => { const i = line.indexOf('='); return [line.slice(0, i), line.slice(i + 1)] }))
const database = privateSettings.MVP_DB_NAME
if (!/^mvp_[a-z0-9_]+$/.test(database || '')) throw Error('An explicitly isolated MVP database is required')
const pg = sql => JSON.parse(execFileSync('docker', [...compose, 'exec', '-T', 'postgres', 'psql', '-X', '-qAt', '-v', 'ON_ERROR_STOP=1', '-U', 'mvp_validation', '-d', database, '-c', sql], { cwd: root, encoding: 'utf8' }).trim())
const run = args => execFileSync(process.execPath, [cli, 'cli', '-s=site-persistence', ...args], { cwd: frontend, encoding: 'utf8', timeout: 300000, maxBuffer: 4 * 1024 * 1024 })
mkdirSync(output, { recursive: true })
let result
try {
  let ready = false
  for (let i = 0; i < 25; i++) { try { const status = await fetch('http://localhost:15173/status/database'); if (status.ok && (await status.json()).connected) { ready = true; break } } catch { /* Backend may still be starting. */ } await new Promise(resolve => setTimeout(resolve, 1000)) }
  if (!ready) throw Error('Start the isolated backend before auditing')
  run(['open', 'http://localhost:15173', '--browser=chrome'])
  result = JSON.parse(run(['--raw', 'run-code', '--filename=scripts/verify-site-persistence.cli.js']).trim())
  result.sourceCommit = execFileSync('git', ['rev-parse', 'HEAD'], { cwd: root, encoding: 'utf8' }).trim()
  result.sourceFingerprint = sourceFingerprint()
  result.database = database
  result.gitPublicationPerformed = false
  if (result.completed) {
    const records = result.records
    for (const [key, value] of Object.entries(records)) if (key.endsWith('Id') && !/^[0-9a-f-]{36}$/.test(value)) throw Error('Invalid fixture identity')
    const sql = `select json_build_object(
      'tool', (select json_build_object('id',tool_id,'name',name,'status',publication_status,'city',service_city) from tools where tool_id='${records.toolId}'),
      'imageCount', (select count(*) from tool_images where tool_id='${records.toolId}'),
      'unit', (select json_build_object('id',equipment_unit_id,'status',operational_status,'serial',serial_number) from equipment_units where equipment_unit_id='${records.unitId}'),
      'reservation', (select json_build_object('id',reservation_id,'status',reservation_status) from reservations where reservation_id='${records.reservationId}'),
      'rentalCount', (select count(*) from rentals where reservation_id='${records.reservationId}' and rental_status='COMPLETED'),
      'deliveryCount', (select count(*) from unit_deliveries where reservation_id='${records.reservationId}'),
      'returnCount', (select count(*) from unit_returns where reservation_id='${records.reservationId}'),
      'approvedExtensions', (select count(*) from reservation_extensions where reservation_id='${records.extensionReservationId}' and extension_status='APPROVED'),
      'cancelledReservation', (select reservation_status='CANCELLED' from reservations where reservation_id='${records.cancelledReservationId}'),
      'confirmedPayments', (select count(*) from payments p join reservations r using(reservation_id) where r.customer_id='${records.customerId}' and p.payment_status='CONFIRMED'),
      'invoiceCount', (select count(*) from invoices where reservation_id='${records.reservationId}'),
      'customer', (select json_build_object('id',user_id,'name',display_name,'hashed',password_hash like '$2%') from users where user_id='${records.customerId}'),
      'managedSupplierDisabled', (select account_status='DISABLED' from users where user_id='${records.managedSupplierId}'),
      'managedAdminDisabled', (select account_status='DISABLED' from users where user_id='${records.managedAdminId}'),
      'selfSupplier', (select count(*) from suppliers where user_id='${records.selfSupplierId}'),
      'managedSupplierRole', (select count(*) from user_roles ur join roles r using(role_id) where ur.user_id='${records.managedSupplierId}' and r.code='SUPPLIER'),
      'managedAdminRole', (select count(*) from user_roles ur join roles r using(role_id) where ur.user_id='${records.managedAdminId}' and r.code='ADMIN'))`
    result.sqlSnapshot = pg(sql)
    const snapshot = result.sqlSnapshot
    const assertions = {
      'PostgreSQL stores tool publication and editing': snapshot.tool?.status === 'PUBLISHED' && snapshot.tool?.name.endsWith(' Edited'),
      'PostgreSQL stores both image associations': snapshot.imageCount === 2,
      'PostgreSQL stores physical unit and activation': snapshot.unit?.status === 'OPERATIONAL',
      'PostgreSQL stores reservation confirmation': snapshot.reservation?.status === 'CONFIRMED',
      'PostgreSQL stores delivery rental and return': snapshot.rentalCount === 1 && snapshot.deliveryCount === 1 && snapshot.returnCount === 1,
      'PostgreSQL stores extension and cancellation': snapshot.approvedExtensions === 1 && snapshot.cancelledReservation,
      'PostgreSQL stores five academic payments': snapshot.confirmedPayments === 5,
      'PostgreSQL stores a unique invoice for the main payment': snapshot.invoiceCount === 1,
      'PostgreSQL stores the edited customer and password hash': snapshot.customer?.name.startsWith('QA Persistent Customer ') && snapshot.customer?.hashed,
      'PostgreSQL stores managed account roles and deactivation': snapshot.managedSupplierDisabled && snapshot.managedAdminDisabled && snapshot.managedSupplierRole === 1 && snapshot.managedAdminRole === 1,
      'PostgreSQL stores the self registered supplier profile': snapshot.selfSupplier === 1,
    }
    for (const [name, passed] of Object.entries(assertions)) { result.checks.push({ name, passed }); if (!passed) { result.completed = false; result.failures.push(name) } }
  }
  result.screenshots = result.screenshots.map(filename => { const bytes = readFileSync(output + filename); return { filename, sha256: createHash('sha256').update(bytes).digest('hex') } })
} catch (error) {
  result ||= { completed: false, checks: [], failures: [] }
  result.completed = false; result.failures.push(error.message)
} finally {
  writeFileSync(output + 'site-persistence.json', JSON.stringify(result, null, 2) + '\n')
  try { run(['close']) } catch { /* Preserve the original audit failure. */ }
}
if (!result.completed) throw Error('Site persistence audit failed: ' + result.failures.join('; '))
console.log('Real browser and PostgreSQL checks passed: ' + result.checks.length)
