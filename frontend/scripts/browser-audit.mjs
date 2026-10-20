import { mkdir, writeFile } from 'node:fs/promises'
import { resolve } from 'node:path'
import { chromium } from '@playwright/test'
import AxeBuilder from '@axe-core/playwright'
import { mockApi, catalog } from '../tests/fixtures.js'

const label = process.argv[2] || 'current'
if (!/^[a-z0-9-]+$/.test(label)) throw new Error('Use a lowercase audit label')
const output = resolve('test-results', label)
await mkdir(output, { recursive: true })
const browser = await chromium.launch({ channel: 'chrome' })
const baseURL = process.env.AUDIT_URL || 'http://localhost:4173'
const report = { scope: 'Local production build; deterministic API fixtures; Chromium desktop/mobile', routes: [] }
try {
  for (const [profile, viewport] of [['desktop', { width: 1440, height: 900 }], ['mobile', { width: 390, height: 844 }]]) {
    const context = await browser.newContext({ viewport, deviceScaleFactor: 1, locale: 'es-CO' })
    const page = await context.newPage()
    await mockApi(page)
    const errors = []
    page.on('pageerror', error => errors.push(error.message))
    const paths = ['/', '/catalogo', `/herramienta/${catalog[1].toolId}`, '/nosotros', '/como-funciona', '/terms-and-conditions', '/cuenta', '/panel/proveedor', '/panel/admin', '/cotizacion', '/pago', '/reserva-confirmada', '/no-existe']
    for (const path of paths) {
      await page.goto(`${baseURL}${path}`)
      await page.locator('h1').first().waitFor()
      await page.evaluate(() => document.fonts.ready)
      // Exercise reveal observers and lazy media before the full-page visual check.
      await page.evaluate(async () => {
        for (let y = 0; y < document.body.scrollHeight; y += 600) {
          window.scrollTo({ top: y, behavior: 'instant' })
          await new Promise(resolve => setTimeout(resolve, 35))
        }
        window.scrollTo({ top: 0, behavior: 'instant' })
      })
      // Visual capture only: complete even off-screen carousel media in both versions.
      // Lighthouse and runtime measurements retain the application's normal loading policy.
      await page.evaluate(() => { for (const image of document.images) image.loading = 'eager' })
      await page.waitForFunction(() => [...document.images].every(image => image.complete), undefined, { timeout: 10000 })
      await page.waitForTimeout(900)
      const axe = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa', 'wcag21aa', 'wcag22aa', 'best-practice']).analyze()
      const key = path === '/' ? 'home' : path.replaceAll('/', '-').slice(1)
      await page.screenshot({ path: resolve(output, `${key}-${profile}.png`), fullPage: true, animations: 'disabled' })
      const geometry = await page.locator('h1, .hero-artwork, .category-section, .featured-section, .catalog-toolbar').evaluateAll(nodes => nodes.map(node => {
        const { x, y, width, height } = node.getBoundingClientRect()
        return { selector: node.className || node.tagName, x: x + scrollX, y: y + scrollY, width, height }
      }))
      const row = { path, profile, horizontalOverflow: await page.evaluate(() => document.documentElement.scrollWidth > innerWidth),
        geometry, violations: axe.violations.map(v => ({ id: v.id, impact: v.impact, description: v.description,
          nodes: v.nodes.map(n => ({ target: n.target, summary: n.failureSummary })) })),
      }
      report.routes.push(row)
      console.log(`${profile} ${path}: ${row.violations.length} axe violations; overflow=${row.horizontalOverflow}`)
    }
    await page.goto(baseURL)
    await page.getByRole('button', { name: 'Ingresar', exact: true }).click()
    await page.getByRole('dialog').waitFor()
    await page.waitForTimeout(900)
    const modalAxe = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa', 'wcag21aa', 'wcag22aa', 'best-practice']).analyze()
    report.routes.push({ path: '/#login-dialog', profile,
      violations: modalAxe.violations.map(v => ({ id: v.id, impact: v.impact,
        nodes: v.nodes.map(n => ({ target: n.target, summary: n.failureSummary })) })),
    })
    await page.goto(`${baseURL}/`)
    const cdp = await context.newCDPSession(page)
    await cdp.send('Performance.enable')
    await cdp.send('HeapProfiler.collectGarbage')
    const before = await cdp.send('Performance.getMetrics')
    if (profile === 'desktop') report.pointer = await page.evaluate(async () => {
      const target = document.querySelector('.hero-artwork')
      const bounds = target.getBoundingClientRect()
      let reads = 0
      const original = target.getBoundingClientRect.bind(target)
      target.getBoundingClientRect = () => { reads++; return original() }
      const mutations = []
      const observer = new MutationObserver(records => mutations.push(...records))
      observer.observe(target, { subtree: true, attributes: true, attributeFilter: ['style'] })
      target.dispatchEvent(new PointerEvent('pointerover', { bubbles: true, pointerType: 'mouse' }))
      for (let i = 0; i < 120; i++) target.dispatchEvent(new PointerEvent('pointermove', {
        bubbles: true, pointerType: 'mouse', clientX: bounds.x + 50 + i, clientY: bounds.y + 100,
      }))
      await new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))
      observer.disconnect()
      target.getBoundingClientRect = original
      return { events: 120, boundsReads: reads, styleMutations: mutations.length }
    })
    for (let i = 0; i < 20; i++) {
      await page.evaluate(() => { history.pushState({}, '', '/catalogo'); dispatchEvent(new PopStateEvent('popstate')) })
      await page.locator('.catalog-page').waitFor()
      await page.evaluate(() => { history.pushState({}, '', '/'); dispatchEvent(new PopStateEvent('popstate')) })
      await page.locator('.home-hero').waitFor()
    }
    await page.waitForTimeout(1000)
    await cdp.send('HeapProfiler.collectGarbage')
    const after = await cdp.send('Performance.getMetrics')
    report[`${profile}Runtime`] = {
      before: Object.fromEntries(before.metrics.map(m => [m.name, m.value])),
      after: Object.fromEntries(after.metrics.map(m => [m.name, m.value])), errors,
      dom: await cdp.send('Memory.getDOMCounters'),
    }
    await context.close()
  }
} finally {
  await browser.close()
  await writeFile(resolve(output, 'browser.json'), JSON.stringify(report, null, 2))
  if (process.argv.includes('--check') && report.routes.some(route => route.violations.length || route.horizontalOverflow)) process.exitCode = 1
}
