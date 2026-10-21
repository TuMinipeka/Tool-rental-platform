import { test, expect } from '@playwright/test'
import { mockApi } from './fixtures.js'

test.beforeEach(async ({ page }) => { await mockApi(page) })

test('first visit plays the boot splash once and then releases the page', async ({ page }) => {
  await page.goto('/', { waitUntil: 'commit' })
  const splash = page.locator('#boot-splash')
  await expect(splash).toHaveAttribute('role', 'status')
  await expect(splash).toContainText('Afinando herramientas')
  await expect(splash).toBeAttached()
  await expect(splash).toHaveCount(0, { timeout: 6000 })
  await expect(page.getByRole('heading', { level: 1 }).first()).toBeVisible()
})

test('returning visitors in the same session only see the splash while loading', async ({ page }) => {
  await page.goto('/')
  await expect(page.locator('#boot-splash')).toHaveCount(0, { timeout: 6000 })
  const started = Date.now()
  await page.reload()
  await expect(page.locator('#boot-splash')).toHaveCount(0, { timeout: 2000 })
  expect(Date.now() - started).toBeLessThan(2000)
})

test('reduced motion keeps the splash static and skips the minimum display time', async ({ page }) => {
  await page.emulateMedia({ reducedMotion: 'reduce' })
  await page.route('**/assets/index-*.js', async route => {
    await new Promise(resolve => setTimeout(resolve, 600))
    await route.continue()
  })
  await page.goto('/', { waitUntil: 'commit' })
  await page.locator('#boot-splash').waitFor()
  const running = await page.evaluate(() => document.getElementById('boot-splash')?.getAnimations({ subtree: true }).length ?? 0)
  expect(running).toBe(0)
  await expect(page.locator('#boot-splash')).toHaveCount(0, { timeout: 2000 })
})
