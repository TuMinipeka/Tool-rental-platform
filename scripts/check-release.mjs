import { readFileSync } from 'node:fs'
const status = JSON.parse(readFileSync('docs/evidence/mvp/status.json', 'utf8'))
const missing = Object.entries(status.publicReleaseGates).filter(([, value]) => value !== true).map(([key]) => key)
if (missing.length) {
  console.error('Public release blocked: ' + missing.join(', '))
  process.exit(1)
}
console.log('All documented public release gates passed. Validate current image digests and private secrets before deployment.')
