import { execFileSync, spawnSync } from 'node:child_process'
import { existsSync, mkdirSync, openSync, closeSync, readFileSync, writeFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import { sourceFingerprint } from './source-fingerprint.mjs'

const root = fileURLToPath(new URL('../', import.meta.url))
process.chdir(root)
mkdirSync('backups', { recursive: true })
mkdirSync('docs/evidence/system-tests', { recursive: true })
const bundled = resolve('backups/tools/inso-13.3.1/inso.exe')
const binary = process.env.INSO_BIN || (existsSync(bundled) ? bundled : 'inso')
const version = execFileSync(binary, ['--version'], { encoding: 'utf8' }).trim()
const rawReport = resolve('backups/insomnia-system-result.json')
const log = openSync('backups/insomnia-system.log', 'w')
let execution
try {
  execution = spawnSync(binary, [
    '--workingDir', resolve('docs/insomnia/system-tests.json'), '--ci',
    'run', 'collection', 'wrk_tool_rental_system', '--env', 'env_system_base',
    '--reporter', 'list', '--output', rawReport,
  ], { stdio: ['ignore', log, log] })
} finally {
  closeSync(log)
}
if (execution.error) throw execution.error
const raw = JSON.parse(readFileSync(rawReport, 'utf8'))
const results = raw.executions.map(item => ({
  request: item.request.name, status: item.response.code,
  durationMs: item.response.responseTime,
  assertions: item.tests.map(test => ({ name: test.testCase, result: test.status })),
}))
const assertions = results.flatMap(item => item.assertions)
const failures = assertions.filter(item => item.result !== 'passed')
const passed = execution.status === 0 && results.length === 41 && assertions.length === 61 && failures.length === 0
const report = {
  testedAt: new Date().toISOString(),
  baseCommit: execFileSync('git', ['rev-parse', 'HEAD'], { encoding: 'utf8' }).trim(),
  workingTreeChangesIncluded: true, sourceFingerprint: sourceFingerprint(),
  engine: `Official Kong Inso CLI ${version}`, graphicalClientExecuted: false,
  target: 'http://localhost:18080', database: 'Isolated PostgreSQL through compose.validation.yml',
  paymentProvider: 'FICTIONAL_GATEWAY', realChargesPerformed: false,
  requests: results.length, assertions: assertions.length, failures: failures.length, passed, results,
}
writeFileSync('docs/evidence/system-tests/insomnia.json', JSON.stringify(report, null, 2) + '\n')
console.log(JSON.stringify({ passed, requests: report.requests, assertions: report.assertions, failures: report.failures }))
if (!passed) process.exitCode = 1
