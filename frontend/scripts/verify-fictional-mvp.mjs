import { chromium, expect } from '@playwright/test'
import { randomUUID } from 'node:crypto'
import { mkdirSync, writeFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { sourceFingerprint } from '../../scripts/source-fingerprint.mjs'
const base = 'http://localhost:15173'
const root = fileURLToPath(new URL('../../docs/evidence/mvp/', import.meta.url))
const evidence = { testedAt: new Date().toISOString(), sourceFingerprint: sourceFingerprint(), scope: 'Real isolated React, Spring Boot/Tomcat and PostgreSQL; fictional payment; no API mocks or external financial calls', checks: [] }
const check = (name, value) => { if (!value) throw new Error(name); evidence.checks.push({ name, passed: true }) }
let browser
const privateValues = []
try {
  let ready = false
  for (let n = 0; n < 60; n++) { try { if ((await fetch(base + '/status/database')).ok) { ready = true; break } } catch { /* Startup */ } await new Promise(resolve => setTimeout(resolve, 1000)) }
  check('Tomcat/backend and PostgreSQL are reachable', ready)
  const features = await (await fetch(base + '/api/v1/public/features')).json()
  check('Fictional checkout works without external credentials', features.hostedCheckout === true && features.paymentProvider === 'FICTIONAL_GATEWAY')
  check('Recovery remains disabled', features.passwordRecovery === false)
  browser = await chromium.launch({ channel: 'chrome' })
  const api = async (actor, path, body, headers = {}) => {
    const response = await actor.context.request.fetch(base + '/api/v1' + path, { method: body ? 'POST' : 'GET', headers: { Authorization: 'Bearer ' + actor.token, ...headers }, ...(body ? { data: body } : {}) })
    if (!response.ok()) throw new Error(path + ': HTTP ' + response.status())
    return response.json()
  }
  const login = async role => {
    const context = await browser.newContext({ viewport: { width: 1440, height: 1000 } })
    const page = await context.newPage()
    await page.goto(base + '/login')
    await page.getByRole('textbox', { name: 'Correo electrónico' }).fill(role + '@toolrental.local')
    await page.getByLabel(/^Contraseña/).fill('ToolRental2026!')
    const response = page.waitForResponse(r => r.url().endsWith('/auth/login') && r.request().method() === 'POST')
    await page.locator('form').getByRole('button', { name: 'Ingresar', exact: true }).click()
    const data = await (await response).json()
    privateValues.push(data.accessToken)
    await expect(page.getByRole('button', { name: 'Abrir mi cuenta' })).toBeVisible()
    return { context, page, token: data.accessToken }
  }
  const supplier = await login('proveedor')
  const customer = await login('cliente')
  const admin = await login('admin')
  check('Three real roles authenticate', Boolean(supplier.token && customer.token && admin.token))
  const categories = await api(supplier, '/categories')
  const nonce = Date.now().toString()
  const tool = await api(supplier, '/tools', { categoryId: categories[0].id.value ?? categories[0].id, name: 'Fictional MVP tool ' + nonce, description: 'Private fictional checkout verification', dailyRate: 25000, currency: 'COP', imageUrls: [base + '/assets/tools/catalog/01.png'], published: true })
  const toolId = tool.id.value ?? tool.id
  const unit = await api(supplier, '/equipment-units', { toolId, inventoryCode: 'F-MVP-' + nonce, serialNumber: 'F-SERIAL-' + nonce, currentCondition: 'GOOD', status: 'OPERATIONAL' })
  const unitId = unit.id.value ?? unit.id
  const date = new Intl.DateTimeFormat('en-CA', { timeZone: 'America/Bogota' }).format(new Date())
  const scheduledAt = new Date(Date.now() + 6000).toISOString()
  const input = { startDate: date, returnDate: date, equipmentUnitIds: [unitId], scheduledFulfillmentAt: scheduledAt, fulfillmentMethod: 'CUSTOMER_PICKUP' }
  const reservation = await api(customer, '/reservations', input)
  const id = reservation.id.value ?? reservation.id
  check('One inclusive day costs the server total of 25000 COP', reservation.total.amount === 25000)
  const overlap = await customer.context.request.post(base + '/api/v1/reservations', { headers: { Authorization: 'Bearer ' + customer.token }, data: input })
  check('An overlapping payment hold is rejected', overlap.status() === 409)
  await customer.page.evaluate(() => { globalThis.history.pushState({}, '', '/cuenta'); globalThis.dispatchEvent(new globalThis.PopStateEvent('popstate')) })
  await customer.page.getByRole('button', { name: 'Actualizar reservas' }).click()
  const row = customer.page.locator('.reservation-row').filter({ hasText: id.slice(0, 8).toUpperCase() })
  const created = customer.page.waitForResponse(r => r.url().endsWith('/payments/checkouts') && r.request().method() === 'POST')
  await row.getByRole('button', { name: 'Pagar en pasarela ficticia' }).click()
  const checkout = await (await created).json()
  await expect(customer.page.getByRole('heading', { name: 'Pasarela ficticia' })).toBeVisible()
  await expect(customer.page.locator('main input')).toHaveCount(0)
  check('Browser opens the fictional checkout without requesting card data', checkout.processor === 'FICTIONAL_GATEWAY' && checkout.amount === 25000)
  mkdirSync(root + 'screenshots', { recursive: true })
  await customer.page.screenshot({ path: root + 'screenshots/fictional-checkout.png', fullPage: true })
  const decisionPath = '/payments/fictional/checkouts/' + checkout.paymentId + '/decision'
  const denied = await supplier.context.request.post(base + '/api/v1' + decisionPath, { headers: { Authorization: 'Bearer ' + supplier.token }, data: { outcome: 'APPROVED' } })
  check('Supplier cannot decide a customer payment', denied.status() === 403)
  await customer.page.getByRole('button', { name: 'Simular aprobación' }).click()
  await expect(customer.page.getByRole('main').getByRole('status')).toHaveText('Pago ficticio aprobado y reserva confirmada')
  check('Fictional approval confirms the real persisted reservation', (await api(customer, '/reservations')).some(item => (item.id.value ?? item.id) === id && item.status === 'CONFIRMED'))
  await api(customer, decisionPath, { outcome: 'APPROVED' })
  const invoices = await api(customer, '/invoices')
  const issued = invoices.filter(item => item.reservationId === id)
  check('Repeated fictional approval creates only one academic invoice', issued.length === 1)
  const doc = await customer.context.request.get(base + '/api/v1/invoices/' + issued[0].invoiceId + '/document', { headers: { Authorization: 'Bearer ' + customer.token } })
  check('Academic document is downloadable and carries a DIAN disclaimer', doc.ok() && (await doc.text()).includes('DIAN'))
  await supplier.page.evaluate(() => { globalThis.history.pushState({}, '', '/panel/proveedor'); globalThis.dispatchEvent(new globalThis.PopStateEvent('popstate')) })
  const management = supplier.page.getByRole('region', { name: 'Gestión de reservas' })
  await management.getByRole('button', { name: 'Actualizar', exact: true }).click()
  const rental = management.locator('article').filter({ hasText: id.slice(0, 8).toUpperCase() })
  await rental.getByText('Entrega y devolución', { exact: true }).click()
  await rental.getByLabel('Dirección de entrega o recogida').fill('Punto académico 123')
  await rental.getByLabel('Ciudad', { exact: true }).fill('Bucaramanga')
  await rental.getByLabel('Departamento', { exact: true }).fill('Santander')
  await rental.getByRole('button', { name: 'Programar entrega' }).click()
  await expect.poll(() => Date.now() >= Date.parse(scheduledAt), { timeout: 10000 }).toBe(true)
  await rental.getByRole('button', { name: 'Completar entrega' }).click()
  await rental.getByRole('button', { name: 'Iniciar alquiler' }).click()
  await rental.getByLabel('Condición de devolución').fill('GOOD')
  await rental.getByRole('button', { name: 'Registrar devolución' }).click()
  await rental.getByRole('button', { name: 'Cerrar alquiler' }).click()
  await expect(rental.locator('.rental-workflow')).toContainText('COMPLETED')
  check('Supplier completes real delivery, rental, return and closure after fictional payment', true)
  const next = new Date(Date.now() + 86400000).toISOString().slice(0, 10)
  const rejectedReservation = await api(customer, '/reservations', { ...input, startDate: next, returnDate: next, scheduledFulfillmentAt: next + 'T14:00:00Z' })
  const rejectedId = rejectedReservation.id.value ?? rejectedReservation.id
  const rejectedCheckout = await api(customer, '/payments/checkouts', { reservationId: rejectedId }, { 'Idempotency-Key': randomUUID() })
  const rejection = await api(customer, '/payments/fictional/checkouts/' + rejectedCheckout.paymentId + '/decision', { outcome: 'DECLINED' })
  check('Fictional rejection releases the held equipment unit', rejection.paymentStatus === 'REJECTED' && Boolean(await api(customer, '/reservations', { ...input, startDate: next, returnDate: next, scheduledFulfillmentAt: next + 'T14:00:00Z' })))
  const report = await api(supplier, '/reports/income?from=' + date + '&to=' + date)
  check('Supplier report is sourced from the backend', report !== null)
  check('Administrator can read actual users', Array.isArray(await api(admin, '/users')))
  check('Customer is denied administrative users', (await customer.context.request.get(base + '/api/v1/users', { headers: { Authorization: 'Bearer ' + customer.token } })).status() === 403)
  for (const actor of [customer, supplier, admin]) check('Web storage contains no access tokens', !(await actor.page.evaluate(() => JSON.stringify({ ...localStorage, ...sessionStorage }))).includes(actor.token))
  await customer.page.screenshot({ path: root + 'screenshots/fictional-payment-result.png', fullPage: true })
  evidence.completed = true
} catch (error) { evidence.completed = false; evidence.failure = error.message; throw error }
finally {
  if (browser) await browser.close()
  const output = JSON.stringify(evidence, null, 2)
  if (privateValues.some(value => value && output.includes(value))) { process.exitCode = 1; console.error('Refusing to write evidence containing a private value') }
  else writeFileSync(root + 'fictional-integration.json', output + '\n')
}
console.log('Real fictional payment workflow checks passed: ' + evidence.checks.length)
