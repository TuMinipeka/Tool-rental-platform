import { mkdir, writeFile } from 'node:fs/promises'
import { resolve } from 'node:path'
import { chromium } from '@playwright/test'
import sharp from 'sharp'
import { mockApi } from '../tests/fixtures.js'

const label = process.argv[2] || 'current'
if (!/^[a-z0-9-]+$/.test(label)) throw new Error('Use a lowercase audit label')
const browser = await chromium.launch({ channel: 'chrome' })
const samples = []
try {
  for (const viewport of [{ width: 1440, height: 900 }, { width: 390, height: 844 }]) {
    for (let run = 1; run <= 3; run++) {
      const context = await browser.newContext({ viewport, deviceScaleFactor: 1, locale: 'es-CO' })
      const page = await context.newPage()
      await mockApi(page)
      await page.goto(process.env.AUDIT_URL || 'http://localhost:4173')
      await page.locator('.hero-artwork img').waitFor()
      await page.waitForTimeout(1200)
      await page.evaluate(async () => {
        await document.fonts.ready
        await Promise.all([...document.images].filter(image => image.loading !== 'lazy').map(image => image.decode().catch(() => {})))
      })
      const cdp = await context.newCDPSession(page)
      await cdp.send('Performance.enable')
      await cdp.send('HeapProfiler.collectGarbage')
      const cold = Object.fromEntries((await cdp.send('Performance.getMetrics')).metrics.map(m => [m.name, m.value]))
      const media = await page.locator('img').evaluateAll(images => images.filter(image => image.complete && image.naturalWidth).map(image => ({
        src: image.currentSrc, width: image.naturalWidth, height: image.naturalHeight,
      })))
      for (const image of media) {
        // naturalWidth is density-corrected for srcset; use the selected file's actual dimensions.
        const metadata = await sharp(resolve('public', `.${new URL(image.src).pathname}`)).metadata()
        image.decodedWidth = metadata.width
        image.decodedHeight = metadata.height
      }
      const rounds = []
      for (let round = 0; round < 3; round++) {
        for (let i = 0; i < 20; i++) {
          for (const [path, selector] of [['/catalogo', '.catalog-page'], ['/', '.home-hero']]) {
            await page.evaluate(path => { history.pushState({}, '', path); dispatchEvent(new PopStateEvent('popstate')) }, path)
            await page.locator(selector).waitFor()
          }
        }
        await page.waitForTimeout(100)
        await cdp.send('HeapProfiler.collectGarbage')
        rounds.push({ ...Object.fromEntries((await cdp.send('Performance.getMetrics')).metrics.map(m => [m.name, m.value])),
          ...await cdp.send('Memory.getDOMCounters') })
      }
      const sample = { viewport, run, cold, media, uniqueDecodedPixelsBytes: [...new Map(media.map(image => [image.src, image])).values()]
        .reduce((sum, image) => sum + image.decodedWidth * image.decodedHeight * 4, 0), rounds }
      samples.push(sample)
      console.log(JSON.stringify({ viewport, run, coldHeap: cold.JSHeapUsedSize,
        decodedPixelBytes: sample.uniqueDecodedPixelsBytes, retainedHeap: rounds.map(r => r.JSHeapUsedSize), nodes: rounds.map(r => r.nodes) }))
      await context.close()
    }
  }
} finally {
  await browser.close()
  await mkdir(resolve('test-results', label), { recursive: true })
  await writeFile(resolve('test-results', label, 'runtime.json'), JSON.stringify(samples, null, 2))
}
