async page => {
  const base = 'http://localhost:15173'
  const output = '../docs/evidence/catalog-sync/'
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
    return tab
  }
  try {
    const name = 'QA Catalog Sync ' + Date.now()
    const customer = await login('cliente@toolrental.local', '/catalogo?q=' + encodeURIComponent(name) + '&municipio=Bucaramanga&existencias=todas')
    await customer.getByRole('button', { name: 'Limpiar filtros' }).waitFor()
    const supplier = await login('proveedor@toolrental.local', '/panel/proveedor')
    await supplier.getByRole('button', { name: 'Herramientas', exact: true }).click()
    const form = supplier.locator('.operations-form form')
    await form.locator('[name=initialQuantity]').fill('0')
    await form.locator('[name=categoryId]').selectOption({ index: 1 })
    await form.locator('[name=name]').fill(name)
    await form.locator('[name=description]').fill('Real browser publication above the former price ceiling')
    await form.locator('[name=dailyRate]').fill('345678')
    await form.locator('[name=published]').uncheck()
    const created = supplier.waitForResponse(r => r.url().endsWith('/api/v1/tools') && r.request().method() === 'POST')
    await form.getByRole('button', { name: 'Crear herramienta', exact: true }).click()
    const tool = await (await created).json()
    result.toolId = tool.id?.value || tool.id
    check('A draft is persisted before publication', Boolean(result.toolId) && !tool.published)
    const images = supplier.getByRole('region', { name: 'Imágenes de ' + name })
    await images.locator('input[type=file]').setInputFiles('public/assets/tools/catalog/01.png')
    await images.getByRole('status').filter({ hasText: 'Imágenes guardadas' }).waitFor()
    await images.screenshot({ path: output + 'supplier-upload.png' })
    result.screenshots.push('supplier-upload.png')
    const article = supplier.locator('.dashboard-list article').filter({ hasText: name })
    await article.getByRole('button', { name: 'Publicar', exact: true }).click()
    await article.getByText('Publicada', { exact: true }).waitFor()
    const publicResponse = await page.context().request.get(base + '/api/v1/public/catalog/' + result.toolId)
    const publication = await publicResponse.json()
    check('The public API returns the real price and stored image', publicResponse.ok() && publication.dailyRate === 345678 && publication.imageUrls.length === 1)
    await customer.bringToFront()
    await customer.getByRole('link', { name, exact: true }).waitFor({ timeout: 40000 })
    check('An already open customer catalog receives the new publication without reloading', await customer.locator('.tool-card').count() === 1)
    check('The price ceiling is opt-in', await customer.locator('.filter-range').textContent().then(value => value.includes('Sin límite')))
    const image = customer.locator('.tool-card img')
    await image.waitFor()
    await image.evaluate(element => element.decode())
    check('The customer receives actual uploaded image bytes', await image.evaluate(element => element.naturalWidth > 0))
    await customer.screenshot({ path: output + 'customer-new-publication.png', fullPage: true })
    result.screenshots.push('customer-new-publication.png')
    await customer.getByRole('link', { name, exact: true }).click()
    await customer.getByRole('button', { name: 'No disponible', exact: true }).waitFor()
    check('Publishing without physical units does not invent stock', await customer.getByRole('button', { name: 'No disponible', exact: true }).isDisabled())
    result.completed = true
  } catch (error) {
    result.completed = false
    result.failure = error.message
  } finally {
    for (const context of contexts) await context.close()
  }
  return result
}
