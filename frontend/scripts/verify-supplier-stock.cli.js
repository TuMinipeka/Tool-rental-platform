async page => {
  const base = 'http://localhost:15173'
  const output = '../docs/evidence/supplier-stock/'
  const contexts = []
  const result = { testedAt: new Date().toISOString(), realServices: true, checks: [], screenshots: [] }
  const check = (name, passed) => { result.checks.push({ name, passed: Boolean(passed) }); if (!passed) throw Error(name) }
  const login = async (email, path) => {
    const context = await page.context().browser().newContext({ viewport: { width: 1440, height: 1000 } })
    contexts.push(context)
    const tab = await context.newPage()
    await tab.goto(base + '/login')
    await tab.locator('[name=email]').fill(email)
    await tab.locator('[name=password]').fill('ToolRental2026!')
    await tab.locator('form').getByRole('button', { name: 'Ingresar', exact: true }).click()
    await tab.getByRole('button', { name: 'Abrir mi cuenta' }).waitFor()
    await tab.goto(base + path)
    return { page: tab, context }
  }
  try {
    const name = 'QA Ready Rental ' + Date.now()
    const customer = await login('cliente@toolrental.local', '/catalogo?municipio=Bucaramanga')
    const supplier = await login('proveedor@toolrental.local', '/panel/proveedor')
    await supplier.page.getByRole('button', { name: 'Herramientas', exact: true }).click()
    const form = supplier.page.locator('.operations-form form')
    await form.locator('[name=categoryId]').selectOption({ index: 1 })
    await form.locator('[name=name]').fill(name)
    await form.locator('[name=description]').fill('A real publication with explicit physical inventory')
    await form.locator('[name=dailyRate]').fill('25000')
    await form.locator('[name=initialQuantity]').fill('2')
    const created = supplier.page.waitForResponse(r => r.url().endsWith('/api/v1/tools') && r.request().method() === 'POST')
    await form.getByRole('button', { name: 'Crear herramienta', exact: true }).click()
    const tool = await (await created).json()
    result.toolId = tool.id?.value || tool.id
    await supplier.page.getByRole('status').filter({ hasText: 'con 2 unidades físicas' }).waitFor()
    const row = supplier.page.locator('.dashboard-list article').filter({ hasText: name })
    check('The supplier sees server persistence and two physical units', (await row.textContent()).includes('2 unidades operativas'))
    const media = supplier.page.getByRole('region', { name: 'Imágenes de ' + name })
    await media.locator('input[type=file]').setInputFiles('public/assets/tools/catalog/01.png')
    await media.getByRole('status').filter({ hasText: 'Imágenes guardadas' }).waitFor()
    await row.screenshot({ path: output + 'supplier-saved.png' })
    result.screenshots.push('supplier-saved.png')
    await row.getByRole('button', { name: 'Ver en catálogo', exact: true }).click()
    await supplier.page.getByRole('link', { name, exact: true }).waitFor()
    check('The supplier can open the exact persisted publication', new URL(supplier.page.url()).searchParams.get('q') === name)
    await customer.page.bringToFront()
    await customer.page.getByRole('link', { name, exact: true }).waitFor({ timeout: 25000 })
    check('The customer receives the publication without a page reload or search', new URL(customer.page.url()).searchParams.get('q') === null)
    const card = customer.page.locator('.tool-card').filter({ hasText: name })
    await card.locator('img').evaluate(element => element.decode())
    check('The public card shows real uploaded image and two units', (await card.textContent()).includes('2 unidades') && await card.locator('img').evaluate(image => image.naturalWidth > 0))
    const catalog = await (await customer.context.request.get(base + '/api/v1/public/catalog')).json()
    check('All matching stocked publications are shown instead of only the first ten', await customer.page.locator('.tool-card').count() === catalog.filter(tool => tool.serviceCity === 'Bucaramanga' && tool.operationalUnitCount > 0).length)
    await card.screenshot({ path: output + 'customer-publication.png' })
    result.screenshots.push('customer-publication.png')
    await supplier.page.goto(base + '/panel/proveedor')
    await supplier.page.getByRole('button', { name: 'Herramientas', exact: true }).click()
    const units = supplier.page.getByRole('region', { name: 'Unidades físicas registradas' })
    const unitRows = units.locator('article').filter({ hasText: name })
    const codes = await unitRows.locator('h3').allTextContents()
    const setStatus = async (code, status) => {
      await units.locator('article').filter({ hasText: code }).getByRole('button', { name: 'Editar unidad', exact: true }).click()
      const editor = units.getByRole('form')
      await editor.getByLabel('Estado operativo').selectOption(status)
      await editor.getByRole('button', { name: 'Guardar unidad', exact: true }).click()
      await editor.waitFor({ state: 'detached' })
    }
    for (const code of codes) await setStatus(code, 'MAINTENANCE')
    await customer.page.bringToFront()
    await customer.page.getByRole('link', { name, exact: true }).waitFor({ state: 'detached', timeout: 25000 })
    check('A publication disappears from the default catalog when stock reaches zero', await customer.page.getByRole('link', { name, exact: true }).count() === 0)
    await customer.page.getByLabel('Solo herramientas con existencias').uncheck()
    await customer.page.getByRole('link', { name, exact: true }).waitFor()
    await customer.page.getByRole('link', { name, exact: true }).click()
    check('An exhausted publication cannot be reserved even in the expanded catalog', await customer.page.getByRole('button', { name: 'No disponible', exact: true }).isDisabled())
    await setStatus(codes[0], 'OPERATIONAL')
    await customer.page.bringToFront()
    await customer.page.getByRole('button', { name: 'Comprobar disponibilidad', exact: true }).waitFor({ timeout: 25000 })
    const date = await customer.page.getByLabel('Fecha de inicio', { exact: true }).inputValue()
    await customer.page.getByLabel('Fecha de devolución', { exact: true }).fill(date)
    await customer.page.getByRole('button', { name: 'Comprobar disponibilidad', exact: true }).click()
    await customer.page.getByLabel('Fecha y hora programada').fill(date + 'T09:00')
    const held = customer.page.waitForResponse(r => r.url().endsWith('/api/v1/reservations') && r.request().method() === 'POST')
    await customer.page.getByRole('button', { name: 'Crear reserva y pagar', exact: true }).click()
    const heldResponse = await held
    const reservation = await heldResponse.json()
    result.reservationId = reservation.id?.value || reservation.id
    check('The customer creates a real held reservation for the new supplier tool', heldResponse.ok() && reservation.status === 'PENDING_PAYMENT' && reservation.total.amount === 25000)
    await customer.page.screenshot({ path: output + 'customer-reservation.png', fullPage: true })
    result.screenshots.push('customer-reservation.png')
    result.completed = true
  } catch (error) {
    result.completed = false
    result.failure = error.message
  } finally {
    for (const context of contexts) await context.close()
  }
  return result
}
