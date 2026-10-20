import { chromium, expect } from '@playwright/test'
import { mkdirSync, writeFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { sourceFingerprint } from '../../scripts/source-fingerprint.mjs'
const base = 'http://localhost:15173'
const root = fileURLToPath(new URL('../../docs/evidence/mvp/', import.meta.url))
const evidence = { testedAt: new Date().toISOString(), sourceFingerprint: sourceFingerprint(), scope: 'Real isolated frontend/backend/PostgreSQL; no API mocks; external checkout acceptance unavailable without credentials', checks: [] }
const check = (name, value) => { if (!value) throw new Error(name); evidence.checks.push({ name, passed: true }) }
let browser
try {
let ready = false
for (let attempt = 0; attempt < 60; attempt++) {
  try { if ((await fetch(base + '/status/database')).ok) { ready = true; break } } catch { /* Wait for the isolated backend after replacement. */ }
  await new Promise(resolve => setTimeout(resolve, 1000))
}
check('Isolated application becomes ready', ready)
const response = await fetch(base + '/status/database')
check('JDBC connection remains active after migration', response.ok && (await response.json()).connected === true)
const features = await (await fetch(base + '/api/v1/public/features')).json()
check('Runtime capability disables recovery', features.passwordRecovery === false)
check('Unconfigured provider is not presented as available', features.hostedCheckout === false)
for (const path of ['/auth/password-recovery', '/auth/password-reset', '/payments/simulations', '/payments/sandbox/webhooks']) {
  const response = await fetch(base + '/api/v1' + path, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: '{}' })
  check(path + ' is disabled', response.status === 404 && (await response.json()).code === 'FEATURE_DISABLED')
}
const spec = await (await fetch(base + '/v3/api-docs')).json()
check('OpenAPI advertises only versioned routes', Object.keys(spec.paths).every(path => path.startsWith('/api/v1/')))
check('OpenAPI excludes disabled recovery and internal approvals', !Object.keys(spec.paths).some(path => /password-reset|password-recovery|sandbox|simulations|internal\/checkouts/.test(path)))
check('OpenAPI publishes hosted checkout', Boolean(spec.paths['/api/v1/payments/checkouts']?.post))
browser = await chromium.launch({ channel: 'chrome', headless: true })
  mkdirSync(root + 'screenshots', { recursive: true })
  for (const [role, email, destination] of [['CUSTOMER', 'cliente@toolrental.local', '/cuenta'], ['SUPPLIER', 'proveedor@toolrental.local', '/panel/proveedor'], ['ADMIN', 'admin@toolrental.local', '/panel/admin']]) {
    const context = await browser.newContext({ viewport: { width: 1440, height: 900 } })
    const page = await context.newPage()
    await page.goto(base + '/login?returnTo=' + encodeURIComponent(destination))
    await expect(page.getByRole('button', { name: '¿Olvidaste tu contraseña?' })).toBeHidden()
    await page.getByRole('textbox', { name: 'Correo electrónico' }).fill(email)
    await page.getByLabel(/^Contraseña/).fill('ToolRental2026!')
    await page.locator('form').getByRole('button', { name: 'Ingresar', exact: true }).click()
    await expect(page).toHaveURL(base + destination)
    await expect(page.getByRole('button', { name: 'Abrir mi cuenta' })).toBeVisible()
    check(role + ' authenticates against real API and renders its view', true)
    const result = await page.evaluate(() => ({ storage: JSON.stringify({ ...localStorage, ...sessionStorage }) }))
    check(role + ' keeps credentials and access tokens out of web storage', !/accessToken|refreshToken|ToolRental2026/.test(result.storage))
    await page.screenshot({ path: root + 'screenshots/hosted-' + role.toLowerCase() + '.png', fullPage: true })
    await context.close()
  }
  const page = await browser.newPage()
  await page.goto(base + '/password-reset?token=untrusted-token')
  await expect(page.getByRole('heading', { name: 'Recuperación no disponible' })).toBeVisible()
  check('Reset URL cannot expose a form while recovery is disabled', true)
  await page.screenshot({ path: root + 'screenshots/recovery-disabled.png', fullPage: true })
  evidence.completed = true
} catch (error) { evidence.completed = false; evidence.failure = error.message; throw error }
finally { if (browser) await browser.close(); writeFileSync(root + 'hosted-integration.json', JSON.stringify(evidence, null, 2) + '\n') }
console.log('Isolated hosted MVP checks passed: ' + evidence.checks.length)
