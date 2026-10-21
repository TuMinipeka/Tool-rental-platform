import { test, expect } from '@playwright/test'
import { mockApi } from './fixtures.js'

test.beforeEach(async ({ page }) => { await mockApi(page) })

test('first visit shows the tape measure long enough to read it, then retracts it', async ({ page }) => {
  // Sample the splash every frame: widest tape and how long it stayed on screen.
  await page.addInitScript(() => {
    window.splashTrace = { maxTape: 0, firstSeen: null, removed: null }
    const sample = () => {
      const tape = document.querySelector('.ts-tape')
      if (!tape) {
        if (window.splashTrace.firstSeen !== null) window.splashTrace.removed = performance.now()
        else requestAnimationFrame(sample)
        return
      }
      window.splashTrace.firstSeen ??= performance.now()
      window.splashTrace.maxTape = Math.max(window.splashTrace.maxTape, tape.getBoundingClientRect().width)
      requestAnimationFrame(sample)
    }
    requestAnimationFrame(sample)
  })
  await page.goto('/', { waitUntil: 'commit' })
  const splash = page.locator('#boot-splash')
  await expect(splash).toHaveAttribute('role', 'status')
  await expect(splash).toContainText('Tomando medidas')
  await expect(splash).toHaveCount(0, { timeout: 5000 })
  await expect(page.getByRole('heading', { level: 1 }).first()).toBeVisible()
  const trace = await page.evaluate(() => window.splashTrace)
  expect(trace.maxTape).toBeGreaterThan(60)
  expect(trace.removed - trace.firstSeen).toBeGreaterThan(1200)
})

test('returning visitors in the same session are not held by the minimum time', async ({ page }) => {
  await page.goto('/')
  await expect(page.locator('#boot-splash')).toHaveCount(0, { timeout: 4000 })
  await page.reload({ waitUntil: 'commit' })
  await expect(page.locator('#boot-splash')).toHaveClass(/boot-splash--returning/)
  await expect(page.locator('#boot-splash')).toHaveCount(0, { timeout: 3000 })
})

test('reduced motion keeps the tape static and skips the minimum time', async ({ page }) => {
  await page.emulateMedia({ reducedMotion: 'reduce' })
  await page.goto('/', { waitUntil: 'commit' })
  await page.locator('#boot-splash').waitFor()
  const running = await page.evaluate(() => document.getElementById('boot-splash')?.getAnimations({ subtree: true }).length ?? 0)
  expect(running).toBe(0)
  await expect(page.locator('#boot-splash')).toHaveCount(0, { timeout: 1500 })
})
