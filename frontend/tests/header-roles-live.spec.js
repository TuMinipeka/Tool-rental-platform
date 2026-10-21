import process from 'node:process'
import { test, expect } from '@playwright/test'
import { mkdirSync } from 'node:fs'

test.skip(process.env.LIVE_SYSTEM_TESTS !== 'true', 'Requires the isolated Docker frontend and real PostgreSQL')
const evidence = '../docs/evidence/header-integration'
const password = 'ToolRental2026!'

for (const [role, email, destination] of [
  ['CUSTOMER', 'cliente@toolrental.local', '/cuenta'],
  ['SUPPLIER', 'proveedor@toolrental.local', '/panel/proveedor'],
  ['ADMIN', 'admin@toolrental.local', '/panel/admin'],
]) {
  test(`real ${role} login preserves header navigation and server permissions`, async ({ page }) => {
    await page.goto(role === 'CUSTOMER' ? '/login?returnTo=%2F%5C%5Cexample.com' : '/login')
    await page.locator('[name=email]').fill(email)
    await page.locator('[name=password]').fill(password)
    const login = page.waitForResponse(response => response.url().endsWith('/auth/login') && response.request().method() === 'POST')
    await page.locator('form').getByRole('button', { name: 'Ingresar', exact: true }).click()
    const response = await login
    expect(response.status()).toBe(200)
    const session = await response.json()
    expect(session.roles).toContain(role)
    await expect(page).toHaveURL(new RegExp(destination + '$'))
    const navigation = page.locator('#main-navigation')
    if (role === 'CUSTOMER') {
      await expect(navigation.getByRole('button', { name: 'Catálogo', exact: true })).toBeVisible()
      await expect(navigation.getByRole('button', { name: 'Administradores', exact: true })).toHaveCount(0)
    } else {
      await expect(navigation.getByRole('button', { name: 'Resumen', exact: true })).toHaveAttribute('aria-current', 'page')
      const label = role === 'ADMIN' ? 'Proveedores' : 'Herramientas'
      await navigation.getByRole('button', { name: label, exact: true }).click()
      await expect(navigation.getByRole('button', { name: label, exact: true })).toHaveAttribute('aria-current', 'page')
      await expect(page.locator('.dashboard-tabs')).toHaveCount(0)
      if (role === 'ADMIN') {
        await navigation.getByRole('button', { name: 'Administradores', exact: true }).click()
        await expect(page.locator('.operations-form form [name=email]')).toBeVisible()
      } else {
        await expect(page.locator('.operations-form form [name=name]')).toBeVisible()
        await expect(page.getByRole('heading', { name: 'Mis herramientas', exact: true })).toBeVisible()
      }
    }
    const users = await page.request.get('/api/v1/users', { headers: { Authorization: `Bearer ${session.accessToken}` } })
    expect(users.status()).toBe(role === 'ADMIN' ? 200 : 403)
    mkdirSync(evidence, { recursive: true })
    await page.screenshot({ path: `${evidence}/${role.toLowerCase()}.png` })
    if (role === 'CUSTOMER') {
      await page.goto('/panel/admin')
      await expect(page.getByText('403', { exact: true })).toBeVisible()
    }
    if (role === 'SUPPLIER') {
      await page.setViewportSize({ width: 390, height: 844 })
      await page.getByRole('button', { name: 'Abrir menú', exact: true }).click()
      await expect(navigation.getByRole('button', { name: 'Herramientas', exact: true })).toBeVisible()
      await navigation.getByRole('button', { name: 'Resumen', exact: true }).click()
      await expect(page).toHaveURL(/\/panel\/proveedor$/)
      await expect(page.getByRole('heading', { name: 'Tu operación, en un solo lugar.', exact: true })).toBeVisible()
      await page.screenshot({ path: `${evidence}/supplier-mobile.png` })
    }
  })
}

test('the login rejects oversized UTF-8 passwords before sending a request', async ({ page }) => {
  await page.goto('/login?returnTo=%2F%5C%5Cexample.com')
  let attempts = 0
  page.on('request', request => { if (request.url().endsWith('/auth/login')) attempts++ })
  await page.locator('[name=email]').fill('cliente@toolrental.local')
  await page.locator('[name=password]').fill('á'.repeat(37))
  await page.locator('form').getByRole('button', { name: 'Ingresar', exact: true }).click()
  await expect(page.getByRole('alert')).toContainText('72 bytes')
  expect(attempts).toBe(0)
})

test('the real API rejects invalid inputs and keeps credential failures generic', async ({ request }) => {
  for (const body of [
    { email: 'a\u200bb@example.test', password },
    { email: 'cliente@toolrental.local', password: 'á'.repeat(37) },
    { email: 'cliente@toolrental.local', password: 'password\0suffix' },
  ]) {
    const response = await request.post('/api/v1/auth/login', { data: body })
    expect(response.status()).toBe(400)
    expect((await response.json()).code).toBe('INVALID_REQUEST')
  }
  const bodies = []
  for (const email of ['cliente@toolrental.local', 'nonexistent-login@example.test']) {
    const response = await request.post('/api/v1/auth/login', { data: { email, password: 'incorrect-passphrase' } })
    expect(response.status()).toBe(401)
    const body = await response.json()
    bodies.push([body.code, body.detail])
  }
  expect(bodies[0]).toEqual(bodies[1])
  expect((await request.get('/api/v1/users')).status()).toBe(401)
})
