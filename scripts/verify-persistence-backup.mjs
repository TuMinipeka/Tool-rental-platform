import { execFileSync } from 'node:child_process'
import { readFileSync, writeFileSync, mkdirSync } from 'node:fs'
import { resolve } from 'node:path'
import { createHash } from 'node:crypto'
import { sourceFingerprint } from './source-fingerprint.mjs'

const settings = Object.fromEntries(readFileSync('.env.validation', 'utf8').split(/\r?\n/).filter(Boolean).map(line => { const i = line.indexOf('='); return [line.slice(0, i), line.slice(i + 1)] }))
const db = settings.MVP_DB_NAME
if (!/^mvp_[a-z0-9_]+$/.test(db || '')) throw Error('An isolated validation database is required')
const compose = ['compose', '--env-file', '.env.validation', '-f', 'compose.validation.yml']
const docker = args => execFileSync('docker', args, { encoding: 'utf8', maxBuffer: 8 * 1024 * 1024 })
const pg = (database, sql) => docker([...compose, 'exec', '-T', 'postgres', 'psql', '-X', '-qAt', '-v', 'ON_ERROR_STOP=1', '-U', 'mvp_validation', '-d', database, '-c', sql]).trim()
const names = JSON.parse(pg(db, "select json_agg(tablename order by tablename) from pg_tables where schemaname='public'"))
const counts = database => Object.fromEntries(names.map(name => [name, Number(pg(database, 'select count(*) from ' + name))]))
const before = counts(db)
const nonce = Date.now()
const restoreDb = 'mvp_restore_' + nonce, restoreVolume = 'mvp_restore_images_' + nonce
const folder = resolve('backups/provider-persistence-' + nonce)
mkdirSync(folder, { recursive: true })
const imageName = 'tool-rental-mvp-backend:working'
const hashes = volumeArgs => docker(['run', '--rm', ...volumeArgs, '--entrypoint', 'sh', imageName, '-c', "find /application/storage/tool-images -type f -exec sha256sum {} + | sort"]).trim()
const actualContainer = docker([...compose, 'ps', '-q', 'backend']).trim()
const volume = JSON.parse(docker(['inspect', actualContainer]))[0].Mounts.find(m => m.Destination === '/application/storage/tool-images').Name
if (!/^tool-rental-mvp_/.test(volume)) throw Error('The image volume must belong to the isolated MVP project')
let stopped = false
try {
  docker([...compose, 'stop', 'backend']); stopped = true
  docker([...compose, 'exec', '-T', 'postgres', 'pg_dump', '-U', 'mvp_validation', '-d', db, '-Fc', '--no-owner', '--no-acl', '-f', '/tmp/provider-persistence.dump'])
  docker([...compose, 'cp', 'postgres:/tmp/provider-persistence.dump', folder + '/database.dump'])
  docker(['run', '--rm', '--user', '0', '-v', volume + ':/application/storage/tool-images:ro', '-v', folder + ':/backup', '--entrypoint', 'tar', imageName, '-C', '/application/storage/tool-images', '-cf', '/backup/images.tar', '.'])
  const beforeHashes = hashes(['-v', volume + ':/application/storage/tool-images:ro'])
  docker([...compose, 'start', 'backend']); stopped = false
  docker([...compose, 'exec', '-T', 'postgres', 'createdb', '-U', 'mvp_validation', restoreDb])
  docker([...compose, 'exec', '-T', 'postgres', 'pg_restore', '-U', 'mvp_validation', '-d', restoreDb, '--no-owner', '--no-acl', '--exit-on-error', '/tmp/provider-persistence.dump'])
  const restoredCounts = counts(restoreDb)
  if (JSON.stringify(before) !== JSON.stringify(restoredCounts)) throw Error('Restored row counts differ')
  docker(['volume', 'create', restoreVolume])
  docker(['run', '--rm', '--user', '0', '-v', restoreVolume + ':/application/storage/tool-images', '-v', folder + ':/backup:ro', '--entrypoint', 'tar', imageName, '-C', '/application/storage/tool-images', '-xf', '/backup/images.tar'])
  const restoredHashes = hashes(['-v', restoreVolume + ':/application/storage/tool-images:ro'])
  if (beforeHashes !== restoredHashes) throw Error('Restored image bytes differ')
  // Recreate only this isolated stack; do not remove its volumes.
  docker([...compose, 'up', '-d', '--force-recreate', 'postgres', 'backend', 'frontend', 'proxy'])
  let connected = false
  for (let attempt = 0; attempt < 50; attempt++) {
    try { const response = await fetch('http://localhost:15173/status/database'); if (response.ok && (await response.json()).connected) { connected = true; break } } catch { /* Wait for the isolated services. */ }
    await new Promise(resolve => setTimeout(resolve, 1000))
  }
  if (!connected) throw Error('Backend did not reconnect after recreation')
  const afterRecreation = counts(db)
  if (JSON.stringify(before) !== JSON.stringify(afterRecreation)) throw Error('Container recreation lost data')
  const records = JSON.parse(readFileSync('docs/evidence/provider-persistence/site-persistence.json', 'utf8')).records
  for (const reference of records.imageReferences.filter(value => value.startsWith('/api/v1/public/tool-images/files/'))) {
    const response = await fetch('http://localhost:15173' + reference)
    if (!response.ok || (await response.arrayBuffer()).byteLength === 0) throw Error('Uploaded image did not survive recreation')
  }
  const tool = await fetch('http://localhost:15173/api/v1/public/catalog/' + records.toolId)
  if (!tool.ok) throw Error('Published tool did not survive recreation')
  writeFileSync('docs/evidence/provider-persistence/backup-restore.json', JSON.stringify({ testedAt: new Date().toISOString(), sourceFingerprint: sourceFingerprint(), database: db, restoredDatabase: restoreDb, restoredImageVolume: restoreVolume, before, afterRestore: restoredCounts, afterRecreation, imageHashes: beforeHashes.split('\n').map(line => line.split('  ')[0]), imageRestorePassed: true, databaseRestorePassed: true, containerRecreationPassed: true, offsiteCopyPassed: false, archiveSha256: Object.fromEntries(['database.dump', 'images.tar'].map(name => [name, createHash('sha256').update(readFileSync(folder + '/' + name)).digest('hex')])) }, null, 2) + '\n')
  console.log('Database and image restoration, recreation and HTTP reads passed.')
} finally {
  if (stopped) docker([...compose, 'start', 'backend'])
}
