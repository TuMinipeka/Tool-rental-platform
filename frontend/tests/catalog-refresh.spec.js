import { test, expect } from '@playwright/test'
import { catalog, mockApi } from './fixtures.js'

const expensiveTool = {
  ...catalog[0], toolId: 'expensive-publication', name: 'New supplier publication',
  categoryName: 'New category', dailyRate: 345678, serviceCity: 'Bucaramanga',
}

test('published prices above the former ceiling are visible without applying a price filter', async ({ page }) => {
  await mockApi(page)
  await page.route('**/public/catalog', route => route.fulfill({ json: [expensiveTool] }))
  await page.goto('/catalogo')
  await expect(page.getByRole('link', { name: expensiveTool.name, exact: true })).toBeVisible()
  await expect(page.locator('.filter-range')).toContainText('Sin límite')
  const price = page.getByRole('slider', { name: 'Precio diario máximo', includeHidden: true })
  await expect(price).toHaveAttribute('max', '350000')
  // The price sidebar is hidden on mobile; the default visibility must still hold.
  if (await price.isVisible()) {
    await price.focus()
    await price.press('Home')
    await expect(page.locator('.tool-card')).toHaveCount(0)
    await page.getByRole('button', { name: 'Limpiar filtros' }).click()
    await expect(page.locator('.tool-card')).toHaveCount(1)
  }
})

test('focus refresh exposes new publications and categories while preserving search and municipality', async ({ page }) => {
  await mockApi(page)
  let publications = []
  await page.route('**/public/catalog', route => route.fulfill({ json: publications }))
  await page.goto('/catalogo?q=New&municipio=Bucaramanga')
  await expect(page.getByRole('button', { name: 'Limpiar filtros' })).toBeVisible()
  publications = [expensiveTool, { ...expensiveTool, toolId: 'other-city', serviceCity: 'Girón' }]
  await page.evaluate(() => window.dispatchEvent(new Event('focus')))
  await expect(page.locator('.tool-card')).toHaveCount(1)
  await expect(page.getByRole('textbox', { name: 'Buscar herramienta o categoría' })).toHaveValue('New')
  await expect(page.locator('.catalog-search-dock select').last()).toContainText('New category')
  await expect(page).toHaveURL(/municipio=Bucaramanga/)
  const price = page.getByRole('slider', { name: 'Precio diario máximo' })
  if (await price.isVisible()) {
    await price.focus()
    await price.press('Home')
    await page.getByRole('button', { name: 'Actualizar catálogo' }).click()
    await expect(price).toHaveValue('0')
    await expect(page.locator('.tool-card')).toHaveCount(0)
  }
})

test('failed refresh keeps previous publications and allows an explicit retry', async ({ page }) => {
  await mockApi(page)
  let fail = false
  await page.route('**/public/catalog', route => route.fulfill(fail
    ? { status: 503, json: { detail: 'Unavailable' } }
    : { json: [expensiveTool] }))
  await page.goto('/catalogo')
  await expect(page.locator('.tool-card')).toHaveCount(1)
  fail = true
  await page.getByRole('button', { name: 'Actualizar catálogo' }).click()
  await expect(page.getByRole('alert')).toContainText('desactualizados')
  await expect(page.locator('.tool-card')).toHaveCount(1)
  fail = false
  await page.getByRole('button', { name: 'Actualizar catálogo' }).click()
  await expect(page.getByRole('alert')).toHaveCount(0)
})

test('navigation and cross-tab invalidation revalidate the public catalog', async ({ page }) => {
  await mockApi(page)
  let publications = [expensiveTool]
  await page.route('**/public/catalog', route => route.fulfill({ json: publications }))
  await page.goto('/')
  await expect(page.locator('.featured-tool-card')).toHaveCount(1)
  publications = []
  const menu = page.getByRole('button', { name: 'Abrir menú' })
  if (await menu.isVisible()) await menu.click()
  await page.locator('#main-navigation').getByRole('button', { name: 'Catálogo', exact: true }).click()
  await expect(page.locator('.tool-card')).toHaveCount(0)
  publications = [expensiveTool]
  await page.evaluate(() => window.dispatchEvent(new StorageEvent('storage', { key: 'tool-rental-catalog-changed' })))
  await expect(page.locator('.tool-card')).toHaveCount(1)
})

test('visible catalog revalidation runs periodically', async ({ page }) => {
  await mockApi(page)
  let publications = [expensiveTool]
  await page.route('**/public/catalog', route => route.fulfill({ json: publications }))
  await page.clock.install()
  await page.goto('/catalogo')
  await expect(page.locator('.tool-card')).toHaveCount(1)
  publications = [{ ...expensiveTool, name: 'Updated publication' }]
  await page.clock.fastForward(30001)
  await expect(page.getByRole('link', { name: 'Updated publication', exact: true })).toBeVisible()
})
