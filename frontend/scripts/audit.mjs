import { mkdir, readFile, writeFile } from 'node:fs/promises'
import { resolve } from 'node:path'
import lighthouse from 'lighthouse'
import desktopConfig from 'lighthouse/core/config/desktop-config.js'
import { launch } from 'chrome-launcher'

const label = process.argv[2] || 'current'
if (!/^[a-z0-9-]+$/.test(label)) throw new Error('Use a lowercase audit label')
const output = resolve('test-results', label)
await mkdir(output, { recursive: true })
const base = process.env.AUDIT_URL || 'http://localhost:4173'
const chrome = await launch({ chromeFlags: ['--headless=new', '--no-sandbox', '--disable-gpu'] })
const summary = []
try {
  for (const [route, profile] of [['/', 'mobile'], ['/', 'desktop'], ['/catalogo', 'mobile']]) {
    for (let run = 1; run <= 3; run++) {
      const key = `${route === '/' ? 'home' : 'catalog'}-${profile}-${run}`
      const options = { port: chrome.port, output: ['json', 'html'], logLevel: 'error',
        onlyCategories: ['performance', 'accessibility', 'best-practices', 'seo'],
      }
      let lhr
      if (process.env.AUDIT_RESUME === 'true') {
        try {
          const saved = JSON.parse(await readFile(resolve(output, `${key}.json`), 'utf8'))
          if (saved.configSettings.formFactor === profile) lhr = saved
        } catch { /* No completed run to resume. */ }
      }
      if (!lhr) {
        const result = await lighthouse(`${base}${route}`, options, profile === 'desktop' ? desktopConfig : undefined)
        await writeFile(resolve(output, `${key}.json`), result.report[0])
        await writeFile(resolve(output, `${key}.html`), result.report[1])
        lhr = result.lhr
      }
      const audits = lhr.audits
      const row = { key, route, profile, run, lighthouse: lhr.lighthouseVersion,
        browser: lhr.environment.hostUserAgent, settings: lhr.configSettings,
        scores: Object.fromEntries(Object.entries(lhr.categories).map(([id, value]) => [id, value.score])),
        fcp: audits['first-contentful-paint'].numericValue,
        lcp: audits['largest-contentful-paint'].numericValue,
        tbt: audits['total-blocking-time'].numericValue,
        cls: audits['cumulative-layout-shift'].numericValue,
        bytes: audits['total-byte-weight'].numericValue,
        failures: Object.values(audits).filter(a => a.score !== null && a.score < 1)
          .map(a => ({ id: a.id, title: a.title, display: a.displayValue, details: a.details })),
      }
      summary.push(row)
      console.log(JSON.stringify({ key, lcp: row.lcp, tbt: row.tbt, cls: row.cls, bytes: row.bytes, scores: row.scores }))
    }
  }
} finally {
  await chrome.kill()
  await writeFile(resolve(output, 'summary.json'), JSON.stringify(summary, null, 2))
}
