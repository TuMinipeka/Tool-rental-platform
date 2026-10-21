async page => {
  const response = await page.context().request.get('http://localhost:8080/api/v1/public/catalog')
  if (!response.ok()) throw Error('The stable API is unavailable')
  const catalog = await response.json()
  const candidates = catalog.filter(tool => tool.dailyRate > 230000).slice(0, 2)
  const result = { testedAt: new Date().toISOString(), readOnly: true, checks: [], screenshots: [] }
  for (const [index, tool] of candidates.entries()) {
    await page.goto('http://localhost:5173/catalogo?' + new URLSearchParams({ q: tool.name, municipio: tool.serviceCity, existencias: 'todas' }))
    await page.getByRole('link', { name: tool.name, exact: true }).waitFor()
    result.checks.push({ toolId: tool.toolId, visibleAboveFormerCeiling: true, municipality: tool.serviceCity, price: tool.dailyRate })
    if (tool.imageUrls.length) {
      const image = page.locator('.tool-card img').first()
      await image.waitFor()
      await image.evaluate(element => element.decode())
      if (!(await image.evaluate(element => element.naturalWidth > 0))) throw Error('The stored image did not load')
    }
    const filename = 'stable-publication-' + (index + 1) + '.png'
    await page.screenshot({ path: '../docs/evidence/catalog-sync/' + filename, fullPage: true })
    result.screenshots.push(filename)
  }
  result.completed = candidates.length > 0 && result.checks.every(check => check.visibleAboveFormerCeiling)
  return result
}
