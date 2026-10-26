import { test, expect } from '@playwright/test'
import { mockApi } from './fixtures.js'

test('customer can view payment history and retry empty or failed responses', async ({ page }) => {
  await mockApi(page)
  let response = { json: [
    { paymentId: 'first', paymentDate: '2026-10-20T15:00:00Z', amount: 25000, status: 'CONFIRMED' },
    { paymentId: 'second', paymentDate: '2026-10-19T15:00:00Z', amount: 10000, status: 'PENDING' },
    { paymentId: 'third', paymentDate: '2026-10-18T15:00:00Z', amount: 10000, status: 'FAILED' },
  ] }
  await page.route('**/api/v1/pagos/cliente/qa-customer', route => route.fulfill(response))
  await page.goto('/login?returnTo=/cuenta')
  await page.getByRole('textbox', { name: 'Correo electrónico' }).fill('qa@example.test')
  await page.getByLabel(/^Contraseña/).fill('qa-password-123456')
  await page.locator('form').getByRole('button', { name: 'Ingresar', exact: true }).click()
  await page.getByRole('button', { name: /Historial de Pagos/ }).click()
  const history = page.getByRole('region', { name: 'Historial de Pagos' })
  await expect(history.getByRole('columnheader')).toHaveText(['Fecha', 'Monto', 'Estado'])
  await expect(history.getByRole('cell', { name: 'Pagado', exact: true })).toBeVisible()
  await expect(history.getByRole('cell', { name: 'Pendiente', exact: true })).toBeVisible()
  await expect(history.getByRole('cell', { name: 'Fallido', exact: true })).toBeVisible()
  response = { status: 503, json: { detail: 'Payment history unavailable' } }
  await history.getByRole('button', { name: 'Actualizar' }).click()
  await expect(history.getByRole('alert')).toContainText('Payment history unavailable')
  response = { json: [] }
  await history.getByRole('button', { name: 'Actualizar' }).click()
  await expect(history).toContainText('Aún no tienes pagos registrados.')
})
