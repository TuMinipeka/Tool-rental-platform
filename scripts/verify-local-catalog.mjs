import { execFileSync } from 'node:child_process'
import { mkdirSync, readFileSync, writeFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'

const root = fileURLToPath(new URL('../', import.meta.url))
const settings = Object.fromEntries(readFileSync(root + '.env', 'utf8').split(/\r?\n/)
  .filter(line => /^[A-Z_]+=/.test(line)).map(line => {
    const separator = line.indexOf('=')
    return [line.slice(0, separator), line.slice(separator + 1).trim().replace(/^(['"])(.*)\1$/, '$2')]
  }))
const sql = `SELECT json_build_object(
  'database', current_database(),
  'users', (SELECT count(*) FROM users),
  'tools', (SELECT count(*) FROM tools),
  'images', (SELECT count(*) FROM tool_images),
  'units', (SELECT count(*) FROM equipment_units),
  'demoMigrations', (SELECT count(*) FROM flyway_schema_history WHERE version IN ('101','118','300','301') AND success),
  'published', (SELECT coalesce(json_agg(json_build_object('id',t.tool_id,'price',t.current_daily_rate,
    'imageCount',(SELECT count(*) FROM tool_images i WHERE i.tool_id=t.tool_id))), '[]'::json)
    FROM tools t WHERE publication_status='PUBLISHED'));`
const snapshot = JSON.parse(execFileSync('docker', ['compose', 'exec', '-T', 'postgres', 'sh', '-c',
  'psql -X -qAt -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB"'],
{ cwd: root, encoding: 'utf8', input: sql }).trim())
const origin = process.env.LOCAL_BACKEND_URL || 'http://localhost:8080'
const response = await fetch(origin + '/api/v1/public/catalog')
if (!response.ok) throw Error('The local catalog request failed: ' + response.status)
const catalog = await response.json()
const checks = {
  nativeBackendUsesSameDatabase: settings.DB_URL === `jdbc:postgresql://localhost:${settings.POSTGRES_PORT || 5432}/${settings.POSTGRES_DB || 'tool_rental'}`,
  demoSeedApplied: snapshot.demoMigrations === 4 && snapshot.users >= 50 && snapshot.tools >= 50,
  allPublishedToolsReachTheApi: catalog.length === snapshot.published.length && snapshot.published.every(row => catalog.some(tool => tool.toolId === row.id)),
  pricesAndImagesMatchPostgres: snapshot.published.every(row => {
    const tool = catalog.find(tool => tool.toolId === row.id)
    return tool && Number(tool.dailyRate) === Number(row.price) && tool.imageUrls.length === row.imageCount
  }),
}
const result = { testedAt: new Date().toISOString(), baseCommit: execFileSync('git', ['rev-parse', 'HEAD'], { cwd: root, encoding: 'utf8' }).trim(),
  database: snapshot.database, databaseHostPort: Number(settings.POSTGRES_PORT || 5432), origin,
  counts: { users: snapshot.users, tools: snapshot.tools, published: catalog.length, images: snapshot.images, units: snapshot.units },
  checks, readOnly: true, completed: Object.values(checks).every(Boolean) }
mkdirSync(root + 'docs/evidence/catalog-sync', { recursive: true })
writeFileSync(root + 'docs/evidence/catalog-sync/database.json', JSON.stringify(result, null, 2) + '\n')
console.log(JSON.stringify(result, null, 2))
if (!result.completed) throw Error('Catalog and local database verification failed')
