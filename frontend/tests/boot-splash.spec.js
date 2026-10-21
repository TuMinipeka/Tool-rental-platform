import { test, expect } from '@playwright/test'
import { mockApi } from './fixtures.js'

test.beforeEach(async ({ page }) => {
  await mockApi(page)
  // Sample the splash every frame: widest tape, when it was removed and whether the app was rendered behind it.
  await page.addInitScript(() => {
    window.splashTrace = { maxTape: 0, removedAt: null, appRenderedBeforeRemoval: false, appEntering: false }
    const sample = () => {
      const tape = document.querySelector('.ts-tape')
      if (document.documentElement.classList.contains('app-entering')) window.splashTrace.appEntering = true
      if (!tape) {
        if (window.splashTrace.maxTape > 0 || document.readyState !== 'loading') {
          window.splashTrace.removedAt ??= performance.now()
          return
        }
      } else {
        window.splashTrace.maxTape = Math.max(window.splashTrace.maxTape, tape.getBoundingClientRect().width)
        if (document.getElementById('root')?.childElementCount) window.splashTrace.appRenderedBeforeRemoval = true
      }
      requestAnimationFrame(sample)
    }
    requestAnimationFrame(sample)
  })
})

test('the splash stays at least 4 s while the app renders behind it, then transitions in', async ({ page }) => {
  await page.goto('/', { waitUntil: 'commit' })
  const splash = page.locator('#boot-splash')
  await expect(splash).toHaveAttribute('role', 'status')
  await expect(splash).toContainText('Tomando medidas')
  await expect(splash).toHaveCount(0, { timeout: 8000 })
  await expect(page.getByRole('heading', { level: 1 }).first()).toBeVisible()
  const trace = await page.evaluate(() => window.splashTrace)
  expect(trace.removedAt).toBeGreaterThanOrEqual(4000)
  expect(trace.maxTape).toBeGreaterThan(150)
  expect(trace.appRenderedBeforeRemoval).toBe(true)
  expect(trace.appEntering).toBe(true)
  await expect(page.locator('html')).not.toHaveClass(/app-entering/)
})

test('reduced motion keeps the tape static but still respects the minimum time', async ({ page }) => {
  await page.emulateMedia({ reducedMotion: 'reduce' })
  await page.goto('/', { waitUntil: 'commit' })
  await page.locator('#boot-splash').waitFor()
  const running = await page.evaluate(() => document.getElementById('boot-splash')?.getAnimations({ subtree: true }).length ?? 0)
  expect(running).toBe(0)
  await expect(page.locator('#boot-splash')).toHaveCount(0, { timeout: 8000 })
  const trace = await page.evaluate(() => window.splashTrace)
  expect(trace.removedAt).toBeGreaterThanOrEqual(4000)
})
