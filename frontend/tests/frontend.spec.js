import { test, expect } from '@playwright/test'
import AxeBuilder from '@axe-core/playwright'
import { catalog, mockApi } from './fixtures.js'

test.beforeEach(async ({ page }) => {
  // Behave as a returning visitor so the first-visit boot splash does not cover interactions.
  await page.addInitScript(() => sessionStorage.setItem('toolrental-splash-seen', '1'))
  await mockApi(page)
})

async function login(page) {
  const returnTo = new URL(page.url()).pathname
  await page.goto('/login?returnTo=' + encodeURIComponent(returnTo))
  await page.getByRole('textbox', { name: 'Correo electrónico' }).fill('qa@example.test')
  await page.getByLabel(/^Contraseña/).fill('qa-password-123456')
  await page.locator('form').getByRole('button', { name: 'Ingresar', exact: true }).click()
  await expect(page).toHaveURL(new RegExp(returnTo + '$'))
  await expect(page.getByRole('button', { name: 'Abrir mi cuenta' })).toBeVisible()
}

for (const path of ['/', '/catalogo', `/herramienta/${catalog[1].toolId}`, '/nosotros', '/como-funciona', '/terms-and-conditions', '/cuenta', '/panel/proveedor', '/panel/admin', '/cotizacion', '/pago', '/reserva-confirmada', '/login', '/registro', '/registro/proveedor', '/password-reset', '/no-existe']) {
  test(`renders ${path} without runtime errors or horizontal overflow`, async ({ page }) => {
    const errors = []
    page.on('pageerror', error => errors.push(error.message))
    await page.goto(path)
    await expect(page.getByRole('heading', { level: 1 }).first()).toBeVisible()
    await expect(page.getByRole('main')).toHaveCount(1)
    await expect.poll(() => page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true)
    expect(errors).toEqual([])
  })
}

test('search keeps municipality and filters the catalog', async ({ page }) => {
  await page.goto('/?municipio=Gir%C3%B3n')
  await page.getByLabel('¿Qué necesitas?').fill('Rotomartillo')
  await page.getByRole('button', { name: 'Buscar', exact: true }).click()
  await expect(page).toHaveURL(/catalogo\?q=Rotomartillo&municipio=Gir/)
  await expect(page.locator('.tool-card')).toHaveCount(1)
  await page.getByRole('textbox', { name: 'Buscar herramienta o categoría' }).fill('no-match-qa')
  await expect(page.getByRole('main')).toHaveCount(1)
  await expect(page.getByRole('button', { name: 'Limpiar filtros' })).toBeVisible()
  await page.getByRole('button', { name: 'Limpiar filtros' }).click()
  await expect(page.locator('.tool-card')).toHaveCount(10)
})

test('catalog pagination, municipality, and price filters retain their contracts', async ({ page }, testInfo) => {
  await page.goto('/catalogo')
  const firstName = await page.locator('.tool-title-link').first().textContent()
  const pagination = page.getByRole('navigation', { name: 'Paginación' })
  await pagination.getByRole('button', { name: '→', exact: true }).click()
  await expect(page.locator('.tool-title-link').first()).not.toHaveText(firstName)
  await expect(page.locator('.tool-card')).toHaveCount(10)
  if (testInfo.project.name === 'desktop') {
    await page.getByRole('combobox', { name: 'Ubicación', exact: true }).selectOption('Girón')
    await expect(page).toHaveURL(/municipio=Gir/)
    const price = page.getByRole('slider', { name: 'Precio diario máximo' })
    await price.focus()
    await price.press('Home')
    await expect(page.locator('.tool-card')).toHaveCount(0)
    await page.getByRole('button', { name: 'Limpiar filtros' }).click()
    await expect(page.locator('.tool-card')).toHaveCount(10)
  }
})

test('mobile menu remains keyboard accessible and closes after navigation', async ({ page }, testInfo) => {
  test.skip(testInfo.project.name !== 'mobile', 'Mobile menu only')
  await page.goto('/')
  const menu = page.getByRole('button', { name: 'Abrir menú' })
  await menu.click()
  await expect(page.getByRole('button', { name: 'Cerrar menú' })).toHaveAttribute('aria-expanded', 'true')
  await page.locator('#main-navigation').getByRole('button', { name: 'Catálogo', exact: true }).click()
  await expect(page).toHaveURL(/\/catalogo(?:\?|$)/)
  await expect(menu).toHaveAttribute('aria-expanded', 'false')
})

test('featured carousel retains next and previous pages', async ({ page }) => {
  await page.goto('/')
  const first = await page.locator('.featured-tool-card h3').first().textContent()
  await page.getByRole('button', { name: 'Ver equipos siguientes' }).click()
  await expect(page.locator('.featured-tool-card h3').first()).not.toHaveText(first)
  await page.getByRole('button', { name: 'Ver equipos anteriores' }).click()
  await expect(page.locator('.featured-tool-card h3').first()).toHaveText(first)
})

test('authentication uses the shared login and registration routes', async ({ page }) => {
  await page.goto('/')
  await page.getByRole('button', { name: 'Ingresar', exact: true }).click()
  await expect(page).toHaveURL(/\/login$/)
  await expect(page.getByRole('dialog')).toHaveCount(0)
  await page.getByRole('button', { name: 'Crear cuenta', exact: true }).click()
  await expect(page).toHaveURL(/\/registro\?returnTo=/)
  await expect(page.getByRole('heading', { name: 'Crea tu cuenta' })).toBeVisible()
})

test('login works while recovery is outside the MVP', async ({ page }) => {
  await page.goto('/')
  await page.getByRole('button', { name: 'Ingresar', exact: true }).click()
  const dialog = page.getByRole('main')
  await expect(dialog.getByRole('button', { name: '¿Olvidaste tu contraseña?' })).toBeHidden()
  await dialog.getByRole('textbox', { name: 'Correo electrónico' }).fill('qa@example.test')
  await dialog.getByLabel(/^Contraseña/).fill('qa-password-123456')
  await dialog.getByRole('button', { name: 'Ingresar', exact: true }).click()
  await expect(page.getByRole('button', { name: 'Abrir mi cuenta' })).toBeVisible()
  expect(await page.evaluate(() => JSON.stringify({ ...localStorage, ...sessionStorage }))).not.toContain('qa-access-token')
})

test('reservation dates stay inclusive and checkout keeps logistics', async ({ page }) => {
  await page.goto(`/herramienta/${catalog[1].toolId}`)
  const start = page.getByLabel('Fecha de inicio')
  const end = page.getByLabel('Fecha de devolución')
  const date = await start.inputValue()
  await end.fill(date)
  await expect(page.locator('.booking-summary')).toContainText('1 día')
  await page.getByRole('button', { name: 'Comprobar disponibilidad' }).click()
  await expect(page).toHaveURL(/\/cotizacion$/)
  await expect(page.getByRole('heading', { level: 1 })).toBeVisible()
  await expect(page.getByRole('button', { name: /Recoger con el proveedor/ })).toBeVisible()
  await expect(page.getByRole('button', { name: /Entrega del proveedor/ })).toBeVisible()
})

test('catalog failures retain the explicitly identified demo experience', async ({ page }) => {
  await mockApi(page, { unavailable: true })
  await page.goto('/catalogo')
  await expect(page.locator('.catalog-demo-label')).toContainText(/demostrativ|muestra/i)
  await expect(page.locator('.tool-card').first()).toBeVisible()
})

test('registration still sends customer profile fields', async ({ page }) => {
  await page.goto('/')
  await page.getByRole('button', { name: 'Ingresar', exact: true }).click()
  const dialog = page.getByRole('main')
  await dialog.getByRole('button', { name: 'Crear cuenta', exact: true }).click()
  await page.waitForTimeout(900)
  expect((await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa', 'wcag21aa', 'wcag22aa', 'best-practice']).analyze()).violations).toEqual([])
  await dialog.getByLabel('Nombre completo').fill('QA Customer')
  await dialog.getByLabel('Número de documento', { exact: true }).fill('12345678')
  await dialog.getByRole('textbox', { name: 'Correo electrónico' }).fill('qa@example.test')
  await dialog.getByLabel(/^Contraseña/).fill('qa-password-123456')
  await dialog.getByLabel(/^Confirmar contraseña/).fill('qa-password-123456')
  await dialog.getByRole('checkbox').check()
  const submission = page.waitForRequest(request => request.url().endsWith('/auth/register'))
  await dialog.getByRole('button', { name: 'Crear cuenta' }).click()
  expect((await submission).postDataJSON()).toMatchObject({ personType: 'PERSON', documentType: 'CC', documentNumber: '12345678', displayName: 'QA Customer' })
  await expect(page.getByRole('button', { name: 'Abrir mi cuenta' })).toBeVisible()
})

test('authenticated checkout creates a request without collecting payment', async ({ page }) => {
  await page.goto(`/herramienta/${catalog[1].toolId}`)
  await login(page)
  const date = await page.getByLabel('Fecha de inicio').inputValue()
  await page.getByRole('button', { name: 'Comprobar disponibilidad' }).click()
  await page.getByRole('textbox', { name: 'Fecha y hora programada' }).fill(`${date}T09:00`)
  await page.waitForTimeout(900)
  const accessibility = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa', 'wcag21aa', 'wcag22aa', 'best-practice']).analyze()
  expect(accessibility.violations.map(v => ({ id: v.id, nodes: v.nodes.map(n => n.target) }))).toEqual([])
  const payments = []
  page.on('request', request => { if (request.url().includes('/payments/')) payments.push(request.url()) })
  const submission = page.waitForRequest(request => request.method() === 'POST' && request.url().endsWith('/api/v1/reservations'))
  await page.getByRole('button', { name: 'Crear solicitud', exact: true }).click()
  expect((await submission).postDataJSON()).toMatchObject({ fulfillmentMethod: 'CUSTOMER_PICKUP', deliveryAddressLine: null })
  await expect(page).toHaveURL(/\/reserva-confirmada$/)
  expect(payments).toEqual([])
})

for (const [role, path] of [['SUPPLIER', '/panel/proveedor'], ['ADMIN', '/panel/admin']]) {
  test(`authenticated ${role} dashboard keeps rendering after code splitting`, async ({ page }) => {
    await mockApi(page, { roles: [role] })
    await page.goto('/')
    await login(page)
    const errors = []
    page.on('pageerror', error => errors.push(error.message))
    await page.evaluate(path => { history.pushState({}, '', path); dispatchEvent(new PopStateEvent('popstate')) }, path)
    await expect(page.locator('.operations-metrics')).toBeVisible()
    await expect(page.getByRole('main')).toHaveCount(1)
    expect(errors).toEqual([])
  })
}

test('public pages and authentication pass automated accessibility checks', async ({ page }) => {
  for (const path of ['/', '/catalogo', '/nosotros', '/terms-and-conditions']) {
    await page.goto(path)
    await expect(page.getByRole('heading', { level: 1 }).first()).toBeVisible()
    await page.waitForTimeout(900) // Audit the settled state, after the existing entry/reveal animations.
    const audit = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa', 'wcag21aa', 'wcag22aa', 'best-practice']).analyze()
    expect(audit.violations.map(v => ({ id: v.id, nodes: v.nodes.map(n => n.target) }))).toEqual([])
  }
  await page.goto('/')
  await page.getByRole('button', { name: 'Ingresar', exact: true }).click()
  await expect(page).toHaveURL(/\/login$/)
  await page.waitForTimeout(900)
  const audit = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa', 'wcag21aa', 'wcag22aa', 'best-practice']).analyze()
  expect(audit.violations.map(v => ({ id: v.id, nodes: v.nodes.map(n => ({ target: n.target, summary: n.failureSummary })) }))).toEqual([])
})

test('secondary routes are downloaded on demand', async ({ page }) => {
  const scripts = []
  page.on('request', request => { if (request.resourceType() === 'script') scripts.push(request.url()) })
  await page.goto('/')
  await expect(page.getByRole('heading', { level: 1 })).toBeVisible()
  expect(scripts.some(url => /TermsAndConditionsPage|RentalPages|AccountPages|InformationPages/.test(url))).toBe(false)
  await page.getByRole('button', { name: 'Términos y condiciones', exact: true }).click()
  await expect(page.getByRole('heading', { level: 1 })).toHaveText('Términos y Condiciones')
  expect(scripts.some(url => url.includes('TermsAndConditionsPage'))).toBe(true)
})

test('hero and featured card reuse one responsive image download', async ({ page }) => {
  const images = new Set()
  page.on('request', request => {
    if (request.url().includes('/assets/optimized/hero-rotary-hammer-1200-')) images.add(request.url())
  })
  await page.goto('/')
  await expect.poll(() => page.locator('.hero-artwork img').evaluate(image => image.complete && image.naturalWidth > 0)).toBe(true)
  await page.locator('.featured-section').scrollIntoViewIfNeeded()
  await expect.poll(() => page.locator('.featured-tool-card img').first().evaluate(image => image.complete && image.naturalWidth > 0)).toBe(true)
  expect(images.size).toBe(1)
})

test('optimized images fall back to original PNGs when unavailable', async ({ page }) => {
  await page.route('**/assets/optimized/*.webp', route => route.fulfill({ status: 404, body: '' }))
  await page.goto('/')
  const image = page.locator('.hero-artwork img')
  await expect.poll(() => image.evaluate(node => node.complete && node.naturalWidth > 0 && node.currentSrc.endsWith('.png'))).toBe(true)
})

test('a failed route download offers recovery and preserves navigation', async ({ page }) => {
  await page.route('**/assets/TermsAndConditionsPage-*.js', route => route.fulfill({ status: 503, body: '' }))
  await page.goto('/')
  await page.getByRole('button', { name: 'Términos y condiciones', exact: true }).click()
  await expect(page.getByRole('alert')).toContainText('No pudimos abrir este contenido')
  await expect(page.getByRole('button', { name: 'Recargar página' })).toBeVisible()
  await expect(page.getByRole('banner')).toBeVisible()
  await expect(page.getByRole('main')).toHaveCount(1)
})

test('normal motion remains enabled and reduced-motion preference is respected', async ({ page }, testInfo) => {
  test.skip(testInfo.project.name === 'mobile', 'Mouse parallax is intentionally excluded on touch devices')
  await page.goto('/')
  await page.waitForTimeout(1000)
  const artwork = page.locator('.hero-artwork')
  const image = artwork.locator('img')
  const initial = await image.evaluate(el => getComputedStyle(el).transform)
  const point = await artwork.evaluate(node => {
    const box = node.getBoundingClientRect()
    for (const xFactor of [.85, .7, .5]) {
      const x = Math.min(innerWidth - 20, box.x + box.width * xFactor)
      const y = box.y + box.height * .5
      if (node.contains(document.elementFromPoint(x, y))) return { x, y }
    }
    return null
  })
  expect(point).not.toBeNull()
  await page.mouse.move(point.x, point.y)
  await expect.poll(() => image.evaluate(el => getComputedStyle(el).transform)).not.toBe(initial)
  await page.emulateMedia({ reducedMotion: 'reduce' })
  await page.goto('/')
  const reduced = await image.evaluate(el => getComputedStyle(el).transform)
  await page.mouse.move(0, 0)
  await page.mouse.move(point.x, point.y)
  await expect.poll(() => image.evaluate(el => getComputedStyle(el).transform)).toBe(reduced)
})

test('card motion preserves mouse feedback, touch scrolling, and keyboard navigation', async ({ page }, testInfo) => {
  await page.goto('/catalogo')
  const card = page.locator('.tool-card').first()
  await card.scrollIntoViewIfNeeded()
  if (testInfo.project.name === 'mobile') {
    await card.dispatchEvent('pointermove', { pointerType: 'touch', clientX: 100, clientY: 100 })
    expect(await card.evaluate(node => node.style.transform)).toBe('')
  } else {
    const box = await card.boundingBox()
    await page.mouse.move(box.x + box.width * .6, box.y + box.height * .5)
    await expect.poll(() => card.evaluate(node => node.style.transform)).toContain('perspective')
    await page.mouse.move(2, 2)
    await expect.poll(() => card.evaluate(node => node.style.transform)).toBe('')
  }
  await card.locator('.tool-title-link').focus()
  await page.keyboard.press('Enter')
  await expect(page).toHaveURL(/\/herramienta\//)
})

test('cancellation preserves the idempotency key and waits for server success', async ({ page }) => {
  const id = '90000000-0000-0000-0000-000000000001'
  let cancelled = false
  const keys = []
  await page.route('**/api/v1/reservations**', async route => {
    if (route.request().method() === 'POST') {
      keys.push(route.request().headers()['idempotency-key'])
      if (keys.length === 1) return route.fulfill({ status: 409, json: { code: 'BUSINESS_CONFLICT', detail: 'Retry the same operation' } })
      cancelled = true
      return route.fulfill({ json: { terminationStatus: 'APPLIED' } })
    }
    return route.fulfill({ json: [{ id, status: cancelled ? 'CANCELLED' : 'PENDING', period: { startDate: '2027-01-01', returnDate: '2027-01-01' }, total: { amount: 25000 } }] })
  })
  await page.goto('/cuenta')
  await login(page)
  const row = page.locator('.reservation-row')
  await row.getByRole('button', { name: 'Solicitar cancelación' }).click()
  await expect(row.getByRole('alert')).toContainText('BUSINESS_CONFLICT')
  await expect(row.locator('.status-pill')).toHaveText('PENDING')
  await row.getByRole('button', { name: 'Solicitar cancelación' }).click()
  await expect(row.locator('.status-pill')).toHaveText('CANCELLED')
  expect(keys).toHaveLength(2)
  expect(keys[0]).toBeTruthy()
  expect(keys[1]).toBe(keys[0])
})


test('recovery route stays disabled even with a token in the URL', async ({ page }) => {
  await page.goto('/password-reset?token=untrusted-token')
  await expect(page.getByRole('heading', { name: 'Recuperación no disponible' })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Guardar nueva contraseña' })).toHaveCount(0)
})

test('provider return parameters never confirm a pending payment', async ({ page }) => {
  const id = '90000000-0000-0000-0000-000000000001'
  await page.route('**/api/v1/payments/checkouts/' + id, route => route.fulfill({ json: { paymentId: id, paymentStatus: 'READY' } }))
  await page.goto('/cuenta')
  await login(page)
  await page.evaluate(id => {
    history.pushState({}, '', '/payment-return?paymentId=' + id + '&status=approved')
    window.dispatchEvent(new PopStateEvent('popstate'))
  }, id)
  await expect(page.getByRole('heading', { name: 'Estado de tu pago' })).toBeVisible()
  await expect(page.getByRole('main').getByRole('status')).toHaveText('Esperando verificación del proveedor')
  await expect(page.getByText('Pago verificado y reserva confirmada')).toHaveCount(0)
})

test('checkout sends only the reservation and redirects to the hosted provider', async ({ page }) => {
  const id = '90000000-0000-0000-0000-000000000001'
  const requests = []
  await page.route('**/api/v1/reservations', route => route.fulfill({ json: [{ id, status: 'PENDING_PAYMENT', period: { startDate: '2027-01-01', returnDate: '2027-01-01' }, total: { amount: 25000 } }] }))
  await page.route('**/api/v1/payments/checkouts', async route => {
    requests.push({ body: route.request().postDataJSON(), key: route.request().headers()['idempotency-key'] })
    await route.fulfill({ json: { paymentId: id, paymentStatus: 'READY', checkoutUrl: 'https://www.mercadopago.com.co/checkout/v1/redirect?pref_id=test' } })
  })
  await page.route('https://www.mercadopago.com.co/**', route => route.fulfill({ contentType: 'text/html', body: '<h1>Checkout de prueba aislado</h1>' }))
  await page.goto('/cuenta')
  await login(page)
  await page.getByRole('button', { name: 'Continuar en Mercado Pago' }).click()
  await expect(page).toHaveURL(/https:\/\/www.mercadopago.com.co\/checkout\//)
  expect(requests).toHaveLength(1)
  expect(requests[0].body).toEqual({ reservationId: id })
  expect(requests[0].key).toBeTruthy()
})
