import { execFileSync } from 'node:child_process'
import { mkdir, readFile, writeFile } from 'node:fs/promises'
import { resolve } from 'node:path'

const before = process.argv[2] || 'baseline'
const after = process.argv[3] || 'optimized'
for (const label of [before, after]) if (!/^[a-z0-9-]+$/.test(label)) throw new Error('Use lowercase audit labels')
const load = async (label, file) => JSON.parse(await readFile(resolve('test-results', label, file), 'utf8'))
const range = values => {
  const sorted = [...values].sort((a, b) => a - b)
  return { median: sorted[Math.floor(sorted.length / 2)], min: sorted[0], max: sorted.at(-1), runs: values }
}
const report = {
  measuredAtUtc: new Date().toISOString(),
  baselineCommit: execFileSync('git', ['rev-parse', 'HEAD'], { encoding: 'utf8' }).trim(),
  scope: 'Local production build on Nginx; localhost:4173; backend localhost:8080 returned an empty catalog; no authenticated session. Browser tests/runtime use deterministic API fixtures.',
  fieldData: 'Unavailable: localhost has no CrUX or production RUM. Lab results are not field Core Web Vitals.',
  lighthouse: {}, bundle: {}, browser: {}, runtime: {},
}
for (const label of [before, after]) {
  const rows = await load(label, 'summary.json')
  report.lighthouse[label] = ['home-mobile', 'home-desktop', 'catalog-mobile'].map(scenario => {
    const runs = rows.filter(row => row.key.startsWith(`${scenario}-`))
    if (runs.length !== 3) throw new Error(`${label}/${scenario} requires three completed navigations`)
    const sample = runs[0]
    if (sample.settings.formFactor !== sample.profile) throw new Error(`Invalid profile: ${label}/${scenario}`)
    return { scenario, route: sample.route, toolVersion: sample.lighthouse, browser: sample.browser,
      formFactor: sample.settings.formFactor, throttling: sample.settings.throttling,
      screen: sample.settings.screenEmulation, throttlingMethod: sample.settings.throttlingMethod,
      cache: 'Cold; Lighthouse storage reset enabled',
      metrics: Object.fromEntries(['fcp', 'lcp', 'tbt', 'cls', 'bytes'].map(key => [key, range(runs.map(run => run[key]))])),
      scores: Object.fromEntries(Object.keys(sample.scores).map(key => [key, range(runs.map(run => run.scores[key]))])),
      remainingAuditIds: [...new Set(runs.flatMap(run => run.failures.map(audit => audit.id)))],
    }
  })
  report.bundle[label] = await load(label, 'bundle.json')
  const browser = await load(label, 'browser.json')
  report.browser[label] = { pointer: browser.pointer,
    routes: browser.routes.map(route => ({ path: route.path, profile: route.profile, overflow: route.horizontalOverflow,
      violations: route.violations.map(violation => violation.id) })),
    runtimeErrors: [...new Set([...browser.desktopRuntime.errors, ...browser.mobileRuntime.errors])],
  }
  const samples = await load(label, 'runtime.json')
  report.runtime[label] = [1440, 390].map(width => {
    const rows = samples.filter(row => row.viewport.width === width)
    return { viewport: rows[0].viewport, coldHeapBytes: range(rows.map(row => row.cold.JSHeapUsedSize)),
      decodedRgbaEstimateBytes: range(rows.map(row => row.uniqueDecodedPixelsBytes)),
      retainedHeapAfterRounds: [0, 1, 2].map(round => range(rows.map(row => row.rounds[round].JSHeapUsedSize))),
      retainedDomNodesAfterRounds: [0, 1, 2].map(round => range(rows.map(row => row.rounds[round].nodes))),
      scriptTimeFor120NavigationsMs: range(rows.map(row => (row.rounds[2].ScriptDuration - row.cold.ScriptDuration) * 1000)),
      note: 'RGBA estimate is width*height*4 of unique selected image files, not measured GPU/process memory. Heap uses CDP GC; each round is 20 home/catalog round trips.' }
  })
}
for (let index = 0; index < report.lighthouse[before].length; index++) {
  const original = report.lighthouse[before][index]
  const candidate = report.lighthouse[after][index]
  for (const key of ['toolVersion', 'browser', 'formFactor', 'throttling', 'screen', 'throttlingMethod']) {
    if (JSON.stringify(original[key]) !== JSON.stringify(candidate[key])) throw new Error(`Non-comparable ${key}: ${original.scenario}`)
  }
}
report.visual = await load(after, 'visual-comparison.json')
report.imageIntegrity = JSON.parse(await readFile('test-results/image-integrity.json', 'utf8'))
const output = resolve('../docs/quality')
await mkdir(output, { recursive: true })
await writeFile(resolve(output, 'frontend-audit-results.json'), JSON.stringify(report, null, 2))
console.log('Saved docs/quality/frontend-audit-results.json')
