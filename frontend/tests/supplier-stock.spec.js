import { test, expect } from '@playwright/test'
import { catalog, mockApi } from './fixtures.js'

test('the catalog shows every matching publication and hides zero stock by default', async ({ page }) => {
  await mockApi(page)
  const publications = Array.from({ length: 14 }, (_, index) => ({ ...catalog[0], toolId: 'stock-' + index, name: 'Published tool ' + index, serviceCity: 'Bucaramanga', operationalUnitCount: index === 13 ? 0 : 1 }))
  await page.route('**/public/catalog', route => route.fulfill({ json: publications }))
  await page.goto('/catalogo')
  await expect(page.locator('.tool-card')).toHaveCount(13)
  await expect(page.getByRole('link', { name: 'Published tool 12', exact: true })).toBeVisible()
  await page.getByLabel('Solo herramientas con existencias').uncheck()
  await expect(page.locator('.tool-card')).toHaveCount(14)
  await page.getByRole('combobox', { name: 'Resultados por página' }).selectOption('10')
  await expect(page.locator('.tool-card')).toHaveCount(10)
  await expect(page.getByRole('navigation', { name: 'Paginación' })).toBeVisible()
})

test('a partial stock failure preserves the saved tool and explains how to complete it', async ({ page }) => {
  await mockApi(page, { roles: ['SUPPLIER'] })
  await page.route('**/categories', route => route.fulfill({ json: [{ id: 'qa-category', name: 'QA Category' }] }))
  let tool = null
  let toolWrites = 0
  await page.route('**/api/v1/tools', async route => {
    if (route.request().method() === 'POST') {
      const input = route.request().postDataJSON()
      tool = { ...input, id: 'saved-tool', supplierId: 'qa-customer', dailyRate: { amount: input.dailyRate } }
      toolWrites++
      await route.fulfill({ status: 201, json: tool })
    } else await route.fulfill({ json: tool ? [tool] : [] })
  })
  await page.route('**/equipment-units', route => route.fulfill(route.request().method() === 'POST'
    ? { status: 503, json: { detail: 'Inventory unavailable' } } : { json: [] }))
  await page.goto('/login?returnTo=%2Fpanel%2Fproveedor')
  await page.locator('[name=email]').fill('qa@example.test')
  await page.locator('[name=password]').fill('qa-password-123456')
  await page.locator('form').getByRole('button', { name: 'Ingresar', exact: true }).click()
  await page.getByRole('button', { name: 'Herramientas', exact: true }).click()
  const form = page.locator('.operations-form form')
  await form.locator('[name=categoryId]').selectOption('qa-category')
  await form.locator('[name=name]').fill('Saved without stock')
  await form.locator('[name=description]').fill('Partial failure must remain visible')
  await form.locator('[name=dailyRate]').fill('25000')
  await form.getByRole('button', { name: 'Crear herramienta' }).click()
  await expect(page.getByRole('alert')).toContainText('La herramienta se guardó en el servidor')
  await expect(page.getByRole('heading', { name: 'Saved without stock', exact: true })).toBeVisible()
  expect(toolWrites).toBe(1)
  await page.getByRole('button', { name: 'Agregar existencias', exact: true }).click()
  await expect(form.locator('[name=toolId]')).toHaveValue('saved-tool')
})
