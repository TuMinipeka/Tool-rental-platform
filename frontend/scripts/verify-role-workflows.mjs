import { execFileSync } from 'node:child_process'
import { createHash } from 'node:crypto'
import { readFileSync, writeFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { sourceFingerprint } from '../../scripts/source-fingerprint.mjs'

const frontend = fileURLToPath(new URL('../', import.meta.url))
const cli = fileURLToPath(new URL('../node_modules/playwright/cli.js', import.meta.url))
const evidence = fileURLToPath(new URL('../../docs/evidence/mvp/', import.meta.url))
const session = 'role-workflows-qa'
const run = args => execFileSync(process.execPath, [cli, 'cli', '-s=' + session, ...args], {
  cwd: frontend, encoding: 'utf8', timeout: 300000, maxBuffer: 4 * 1024 * 1024,
})

const status = await fetch('http://localhost:15173/status/database')
if (!status.ok || !(await status.json()).connected) throw new Error('Start the isolated validation stack on localhost:15173 first')
console.log('Waiting 61 seconds for the shared authentication rate-limit window; backend limits remain unchanged.')
await new Promise(resolve => setTimeout(resolve, 61000))
let result
try {
  run(['open', 'http://localhost:15173', '--browser=chrome'])
  run(['resize', '1440', '1000'])
  result = JSON.parse(run(['--raw', 'run-code', '--filename=scripts/verify-role-workflows.cli.js']).trim())
  result.sourceCommit = execFileSync('git', ['rev-parse', 'HEAD'], { cwd: frontend, encoding: 'utf8' }).trim()
  result.sourceFingerprint = sourceFingerprint()
  result.publicDeploymentPerformed = false
  result.screenshots = result.screenshots.map(filename => {
    const bytes = readFileSync(evidence + 'screenshots/' + filename)
    return { filename, sha256: createHash('sha256').update(bytes).digest('hex'), width: bytes.readUInt32BE(16), height: bytes.readUInt32BE(20) }
  })
  writeFileSync(evidence + 'role-workflows.json', JSON.stringify(result, null, 2) + '\n')
} finally {
  run(['close'])
}
if (!result.completed || result.checks.some(check => !check.passed)) throw new Error('Role workflows failed: ' + result.failures.join('; '))
console.log('Playwright CLI real role workflow checks passed: ' + result.checks.length)
console.log('Screenshots recorded: ' + result.screenshots.length)
