async page => {
  const base = 'http://localhost:5173'
  const result = { testedAt: new Date().toISOString(), base, businessDataWrites: false, checks: [] }
  const response = await page.context().request.get('http://localhost:8080/api/v1/public/catalog')
  if (!response.ok()) throw Error('The stable API is unavailable')
  const catalog = await response.json()
  const city = 'Bucaramanga'
  await page.goto(base + '/catalogo?' + new URLSearchParams({ municipio: city }))
  await page.getByRole('combobox', { name: 'Resultados por página' }).waitFor()
  const expected = catalog.filter(tool => tool.serviceCity === city && tool.operationalUnitCount > 0).length
  await page.waitForFunction(expected => document.querySelectorAll('.tool-card').length === expected, expected)
  result.checks.push({ name: 'The deployed default catalog shows every stocked publication', passed: true, count: expected })
  const withoutStock = catalog.find(tool => tool.serviceCity === city && tool.operationalUnitCount === 0)
  if (withoutStock) {
    await page.getByLabel('Solo herramientas con existencias').uncheck()
    await page.getByRole('textbox', { name: 'Buscar herramienta o categoría' }).fill(withoutStock.name)
    await page.getByRole('link', { name: withoutStock.name, exact: true }).waitFor()
    result.checks.push({ name: 'The existing zero-stock publication is saved and accessible through the explicit filter', passed: true, toolId: withoutStock.toolId })
  }
  await page.screenshot({ path: '../docs/evidence/supplier-stock/stable-catalog.png', fullPage: true })
  await page.setViewportSize({ width: 390, height: 844 })
  await page.reload()
  result.checks.push({ name: 'The updated catalog remains usable on mobile without horizontal overflow', passed: await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth) })
  result.completed = result.checks.every(check => check.passed)
  return result
}
