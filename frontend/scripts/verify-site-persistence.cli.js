async page => {
  const base = 'http://localhost:15173'
  const output = '../docs/evidence/provider-persistence/'
  const result = { testedAt: new Date().toISOString(), tool: 'Playwright CLI', realServices: true, checks: [], screenshots: [], records: {}, failures: [] }
  const contexts = [], errors = []
  const nonce = Date.now().toString()
  const id = value => value?.value ?? value
  const check = (name, value) => { result.checks.push({ name, passed: Boolean(value) }); if (!value) throw Error(name) }
  const capture = async (tab, name) => {
    if (name === 'supplier-images.png') {
      const region = tab.getByRole('region', { name: /^Imágenes de / })
      await region.evaluate(element => element.scrollIntoView({ block: 'center' }))
      await region.screenshot({ path: output + name })
    } else if (name === 'customer-invoice.png') {
      // Chromium's native PDF viewer paints asynchronously after the iframe loads.
      await tab.waitForTimeout(2000)
      await tab.getByRole('dialog').screenshot({ path: output + name })
    } else if (name === 'supplier-inventory.png') {
      await tab.locator('.operations-form').screenshot({ path: output + name })
    } else await tab.screenshot({ path: output + name, fullPage: true })
    result.screenshots.push(name)
  }
  const nav = async (actor, path) => actor.page.evaluate(path => { history.pushState({}, '', path); dispatchEvent(new PopStateEvent('popstate')) }, path)
  const response = async (actor, path, method = 'GET', data, headers = {}) => {
    const http = await (actor?.context || page.context()).request.fetch(base + '/api/v1' + path, { method, headers: { ...(actor ? { Authorization: 'Bearer ' + actor.token } : {}), ...headers }, ...(data ? { data } : {}) })
    let body = null; try { body = await http.json() } catch { /* Binary and empty responses are checked separately. */ }
    return { status: http.status(), body }
  }
  const api = async (...args) => { const r = await response(...args); if (r.status >= 400) throw Error(args[1] + ': ' + r.status + ' ' + r.body?.code); return r.body }
  const actorPage = async () => {
    const context = await page.context().browser().newContext({ viewport: { width: 1440, height: 1000 }, timezoneId: 'America/Bogota' })
    contexts.push(context)
    const tab = await context.newPage(); tab.on('pageerror', error => errors.push(error.message))
    return { context, page: tab }
  }
  const bind = (actor, session) => {
    check('Authenticated account has access token', Boolean(session.accessToken))
    actor.token = session.accessToken; actor.userId = session.userId
    actor.page.on('response', async r => { if (r.url().endsWith('/auth/refresh') && r.ok()) actor.token = (await r.json()).accessToken })
    return actor
  }
  const login = async (email, password = 'ToolRental2026!') => {
    const actor = await actorPage(); await actor.page.goto(base + '/login')
    await actor.page.locator('[name=email]').fill(email); await actor.page.locator('[name=password]').fill(password)
    const wait = actor.page.waitForResponse(r => r.url().endsWith('/auth/login') && r.request().method() === 'POST')
    await actor.page.locator('form').getByRole('button', { name: 'Ingresar', exact: true }).click()
    return bind(actor, await (await wait).json())
  }
  const register = async supplier => {
    const actor = await actorPage(), tab = actor.page
    const email = `qa.${supplier ? 'supplier' : 'buyer'}.${nonce}@example.test`
    await tab.goto(base + (supplier ? '/registro/proveedor' : '/registro'))
    await tab.locator('[name=displayName]').fill('QA ' + (supplier ? 'Supplier ' : 'Buyer ') + nonce)
    await tab.locator('[name=email]').fill(email); await tab.locator('[name=password]').fill('Private-QA-2026!'); await tab.locator('[name=confirmPassword]').fill('Private-QA-2026!')
    await tab.locator('[name=documentNumber]').fill('QA-' + nonce)
    if (supplier) await tab.locator('[name=businessName]').fill('QA Supplier Business ' + nonce)
    check('Terms are not preselected', !(await tab.locator('[name=terms]').isChecked()))
    await tab.locator('[name=terms]').check()
    const wait = tab.waitForResponse(r => r.url().endsWith(supplier ? '/auth/supplier-registration' : '/auth/register') && r.request().method() === 'POST')
    await tab.locator('form').getByRole('button', { name: 'Crear cuenta', exact: true }).click()
    const session = await (await wait).json()
    check('Self registration assigns only its intended role', session.roles?.length === 1 && session.roles[0] === (supplier ? 'SUPPLIER' : 'CUSTOMER'))
    actor.email = email; return bind(actor, session)
  }
  try {
    check('Anonymous tool creation is rejected', (await response(null, '/tools', 'POST', {})).status === 401)
    const supplier = await login('proveedor@toolrental.local'), admin = await login('admin@toolrental.local'), customer = await register(false)
    result.records.customerId = customer.userId
    await supplier.page.getByRole('button', { name: 'Herramientas', exact: true }).click()
    const form = supplier.page.locator('.operations-form form'), name = 'QA Persistent Drill ' + nonce
    await form.locator('[name=categoryId]').selectOption({ index: 1 }); await form.locator('[name=name]').fill(name)
    await form.locator('[name=description]').fill('Created in browser and verified in PostgreSQL'); await form.locator('[name=dailyRate]').fill('25000'); await form.locator('[name=published]').uncheck()
    const created = supplier.page.waitForResponse(r => r.url().endsWith('/api/v1/tools') && r.request().method() === 'POST')
    await form.getByRole('button', { name: 'Crear herramienta', exact: true }).click()
    const tool = await (await created).json(), toolId = id(tool.id); result.records.toolId = toolId
    check('Supplier creates an unpublished tool through UI', Boolean(toolId) && !tool.published)
    check('Draft is absent from public catalog', (await response(null, '/public/catalog/' + toolId)).status === 404)
    const media = supplier.page.getByRole('region', { name: 'Imágenes de ' + name })
    await media.locator('input[type=file]').setInputFiles('public/assets/tools/catalog/01.png')
    await media.getByRole('status').filter({ hasText: 'Imágenes guardadas' }).waitFor()
    const firstImage = (await api(supplier, '/tools/' + toolId + '/images'))[0].imageUrl
    check('Image bytes are publicly readable', (await supplier.context.request.get(base + firstImage)).ok())
    await media.getByLabel('URL de imagen').fill(base + '/assets/tools/catalog/02.png'); await media.getByRole('button', { name: 'Añadir URL', exact: true }).click()
    await media.locator('figure').nth(1).waitFor()
    check('Uploaded and external images have stored associations', (await api(supplier, '/tools/' + toolId + '/images')).length === 2)
    await capture(supplier.page, 'supplier-images.png')
    await media.locator('figure').first().getByRole('button', { name: 'Retirar imagen' }).click()
    await media.locator('figure').nth(1).waitFor({ state: 'detached' })
    check('Removing an uploaded image removes its bytes', (await supplier.context.request.get(base + firstImage)).status() === 404)
    await media.locator('input[type=file]').setInputFiles('public/assets/tools/catalog/01.png')
    await media.locator('figure').nth(1).waitFor()
    result.records.imageReferences = (await api(supplier, '/tools/' + toolId + '/images')).map(row => row.imageUrl)
    const article = supplier.page.locator('.dashboard-list article').filter({ hasText: name })
    await article.getByRole('button', { name: 'Publicar', exact: true }).click(); await article.getByText('Publicada', { exact: true }).waitFor()
    check('Publishing makes the persisted tool public', (await api(null, '/public/catalog/' + toolId)).name === name)
    await article.getByRole('button', { name: 'Retirar', exact: true }).click(); await article.getByText('No publicada', { exact: true }).waitFor()
    check('Withdrawing removes the public tool without deleting history', (await response(null, '/public/catalog/' + toolId)).status === 404 && (await api(supplier, '/tools/' + toolId)).name === name)
    await article.getByRole('button', { name: 'Publicar', exact: true }).click(); await article.getByText('Publicada', { exact: true }).waitFor()
    await article.getByRole('button', { name: 'Editar', exact: true }).click(); await form.locator('[name=name]').fill(name + ' Edited')
    await form.getByRole('button', { name: 'Guardar cambios', exact: true }).click(); await supplier.page.getByRole('heading', { name: name + ' Edited', exact: true }).first().waitFor()
    check('Editing is readable from a fresh API query', (await api(supplier, '/tools/' + toolId)).name === name + ' Edited')
    await supplier.page.getByRole('button', { name: 'Unidad física', exact: true }).click()
    await form.locator('[name=toolId]').selectOption(toolId); await form.locator('[name=inventoryCode]').fill('QA-PERSIST-' + nonce); await form.locator('[name=serialNumber]').fill('SERIAL-' + nonce)
    const unitWait = supplier.page.waitForResponse(r => r.url().endsWith('/equipment-units') && r.request().method() === 'POST')
    await form.getByRole('button', { name: 'Registrar unidad', exact: true }).click()
    const unit = await (await unitWait).json(), unitId = id(unit.id); result.records.unitId = unitId
    check('Physical unit is created and operational', unit.status === 'OPERATIONAL')
    const units = supplier.page.getByRole('region', { name: 'Unidades físicas registradas' }), unitRow = units.locator('article').filter({ hasText: 'QA-PERSIST-' + nonce })
    for (const status of ['MAINTENANCE', 'OPERATIONAL']) {
      await unitRow.getByRole('button', { name: 'Editar unidad', exact: true }).click()
      const editor = units.getByRole('form'); await editor.getByLabel('Estado operativo').selectOption(status)
      await editor.getByRole('button', { name: 'Guardar unidad' }).click(); await editor.waitFor({ state: 'detached' })
      check('Unit status ' + status + ' is persisted', (await api(supplier, '/equipment-units/' + unitId)).status === status)
    }
    await capture(supplier.page, 'supplier-inventory.png')
    await supplier.page.reload(); await supplier.page.getByRole('button', { name: 'Herramientas', exact: true }).click()
    check('Tool survives a browser reload', await supplier.page.getByRole('heading', { name: name + ' Edited', exact: true }).count() === 1)
    check('Customer cannot update supplier tool', (await response(customer, '/tools/' + toolId + '/publication', 'PATCH', { published: false })).status === 403)
    check('Customer cannot access image management', (await response(customer, '/tools/' + toolId + '/images')).status === 403)
    check('Customer cannot list administrative accounts', (await response(customer, '/users')).status === 403)
    const today = new Intl.DateTimeFormat('en-CA', { timeZone: 'America/Bogota' }).format(new Date())
    const scheduled = new Date(Date.now() + 12000)
    await customer.page.goto(base + '/herramienta/' + toolId); await customer.page.getByRole('button', { name: 'Abrir mi cuenta' }).waitFor()
    await customer.page.getByLabel('Fecha de inicio', { exact: true }).fill(today); await customer.page.getByLabel('Fecha de devolución', { exact: true }).fill(today)
    await customer.page.getByRole('button', { name: 'Comprobar disponibilidad' }).click()
    await customer.page.getByLabel('Fecha y hora programada').fill(new Date(scheduled.getTime() - 5 * 3600000).toISOString().slice(0, 19))
    const reservationWait = customer.page.waitForResponse(r => r.url().endsWith('/api/v1/reservations') && r.request().method() === 'POST')
    await customer.page.getByRole('button', { name: 'Crear reserva y pagar', exact: true }).click()
    const reservation = await (await reservationWait).json(), reservationId = id(reservation.id); result.records.reservationId = reservationId
    check('Real quote creates a held reservation at the server price', reservation.status === 'PENDING_PAYMENT' && reservation.total.amount === 25000)
    const checkoutWait = customer.page.waitForResponse(r => r.url().endsWith('/payments/checkouts') && r.request().method() === 'POST')
    await customer.page.getByRole('button', { name: 'Pagar con Visa', exact: true }).click()
    const checkout = await (await checkoutWait).json(); result.records.paymentId = checkout.paymentId
    await customer.page.getByRole('main').getByRole('status').filter({ hasText: 'confirmada' }).waitFor()
    check('Visa academic approval confirms the stored reservation', (await api(customer, '/reservations/' + reservationId)).status === 'CONFIRMED')
    await capture(customer.page, 'customer-payment.png')
    const invoices = (await api(customer, '/invoices')).filter(row => row.reservationId === reservationId)
    check('Payment creates exactly one academic invoice', invoices.length === 1); result.records.invoiceId = invoices[0].invoiceId
    await nav(customer, '/cuenta'); await customer.page.getByRole('button', { name: /^Comprobantes/ }).click()
    const downloads = customer.page.waitForEvent('download'); await customer.page.getByRole('button', { name: 'Descargar PDF', exact: true }).click()
    check('Customer downloads a PDF', (await downloads).suggestedFilename().endsWith('.pdf'))
    await customer.page.getByRole('button', { name: 'Vista previa', exact: true }).click(); await customer.page.getByRole('dialog').waitFor()
    await capture(customer.page, 'customer-invoice.png'); await customer.page.getByRole('button', { name: 'Cerrar vista previa' }).click()
    await nav(supplier, '/panel/proveedor'); await supplier.page.getByRole('button', { name: 'Resumen', exact: true }).click()
    const management = supplier.page.getByRole('region', { name: 'Gestión de reservas' }); await management.getByRole('button', { name: 'Actualizar', exact: true }).click()
    const rental = management.locator('article').filter({ hasText: reservationId.slice(0, 8).toUpperCase() })
    await rental.getByText('Entrega y devolución', { exact: true }).click(); await rental.getByLabel('Dirección de entrega o recogida').fill('Punto académico QA')
    await rental.getByLabel('Ciudad', { exact: true }).fill('Bucaramanga'); await rental.getByLabel('Departamento', { exact: true }).fill('Santander'); await rental.getByRole('button', { name: 'Programar entrega' }).click()
    await supplier.page.waitForFunction(timestamp => Date.now() >= timestamp, scheduled.getTime())
    await rental.getByRole('button', { name: 'Completar entrega', exact: true }).click(); await rental.getByRole('button', { name: 'Iniciar alquiler', exact: true }).click()
    await rental.getByLabel('Condición de devolución').fill('GOOD'); await rental.getByLabel('Notas de devolución').fill('Returned through the real UI')
    await rental.getByRole('button', { name: 'Registrar devolución', exact: true }).click(); await rental.getByRole('button', { name: 'Cerrar alquiler', exact: true }).click()
    await rental.getByText('COMPLETED', { exact: true }).waitFor(); check('Rental lifecycle is persisted', (await api(supplier, '/rentals/' + reservationId)).rentalStatus === 'COMPLETED')
    await capture(supplier.page, 'supplier-completed-rental.png')
    const reports = supplier.page.getByRole('region', { name: 'Reportes operativos' })
    for (const kind of ['income', 'most-rented-tools', 'frequent-customers', 'availability', 'profitability']) {
      await reports.locator('[name=kind]').selectOption(kind)
      const wait = supplier.page.waitForResponse(r => r.url().includes('/reports/' + kind + '?'))
      await reports.getByRole('button', { name: 'Consultar reporte' }).click(); const http = await wait
      check('Real supplier report ' + kind, http.ok() && Array.isArray(await http.json()))
    }
    await capture(supplier.page, 'supplier-reports.png')
    // Exercise the four other fixed methods on separate future reservations, without colliding with the completed rental.
    let extensionReservation
    for (const [index, label] of ['Mastercard', 'PSE', 'Mercado Pago', 'PayPal'].entries()) {
      const date = new Date(Date.now() + (index + 3) * 86400000).toISOString().slice(0, 10)
      const next = await api(customer, '/reservations', 'POST', { equipmentUnitIds: [unitId], startDate: date, returnDate: date, scheduledFulfillmentAt: date + 'T14:00:00Z', fulfillmentMethod: 'CUSTOMER_PICKUP' })
      extensionReservation = next
      await nav(customer, '/pago?reservationId=' + id(next.id))
      await customer.page.getByRole('button', { name: new RegExp('^' + label + ' ') }).click()
      const wait = customer.page.waitForResponse(r => r.url().endsWith('/payments/checkouts') && r.request().method() === 'POST')
      await customer.page.getByRole('button', { name: 'Pagar con ' + label, exact: true }).click(); const stored = await (await wait).json()
      await customer.page.getByRole('main').getByRole('status').filter({ hasText: 'confirmada' }).waitFor()
      check(label + ' academic payment is stored', (await api(customer, '/payments/checkouts/' + stored.paymentId)).paymentStatus === 'DONE' && (await api(customer, '/reservations/' + id(next.id))).status === 'CONFIRMED')
    }
    await nav(customer, '/cuenta')
    const extendedId = id(extensionReservation.id)
    const extensionRow = customer.page.locator('.reservation-row').filter({ hasText: extendedId.slice(0, 8).toUpperCase() })
    await extensionRow.getByRole('button', { name: 'Solicitar extensión', exact: true }).click()
    const extendedDate = new Date(new Date(extensionReservation.period.returnDate + 'T12:00:00Z').getTime() + 86400000).toISOString().slice(0, 10)
    await extensionRow.locator('[name=requestedReturnDate]').fill(extendedDate)
    const extensionWait = customer.page.waitForResponse(r => r.url().includes('/reservation-extensions/reservations/') && r.request().method() === 'POST')
    await extensionRow.getByRole('button', { name: 'Enviar solicitud', exact: true }).click()
    const extension = await (await extensionWait).json(); result.records.extensionReservationId = extendedId
    check('Customer requests an extension through UI', extension.status === 'REQUESTED')
    await api(supplier, '/reservation-extensions/' + id(extension.id) + '/approval', 'POST', {})
    check('Supplier approval persists the effective return date', (await api(customer, '/reservations/' + extendedId)).effectiveReturnDate === extendedDate)
    const cancellationDate = new Date(Date.now() + 12 * 86400000).toISOString().slice(0, 10)
    const cancellable = await api(customer, '/reservations', 'POST', { equipmentUnitIds: [unitId], startDate: cancellationDate, returnDate: cancellationDate, scheduledFulfillmentAt: cancellationDate + 'T14:00:00Z', fulfillmentMethod: 'CUSTOMER_PICKUP' })
    result.records.cancelledReservationId = id(cancellable.id)
    await customer.page.getByRole('button', { name: 'Actualizar reservas', exact: true }).click()
    const cancelRow = customer.page.locator('.reservation-row').filter({ hasText: id(cancellable.id).slice(0, 8).toUpperCase() })
    await cancelRow.getByRole('button', { name: 'Solicitar cancelación', exact: true }).click(); await cancelRow.getByText('CANCELLED', { exact: true }).waitFor()
    check('Customer cancellation is persisted', (await api(customer, '/reservations/' + id(cancellable.id))).status === 'CANCELLED')
    await customer.page.getByRole('button', { name: /^Perfil y seguridad/ }).click()
    const profile = customer.page.locator('.profile-security form').first()
    await profile.getByLabel('Nombre completo').fill('QA Persistent Customer ' + nonce); await profile.getByRole('button', { name: 'Guardar cambios', exact: true }).click()
    await customer.page.getByText('Tus datos se actualizaron correctamente.', { exact: true }).waitFor()
    check('Customer profile update is persisted', (await api(customer, '/users/me')).displayName === 'QA Persistent Customer ' + nonce)
    await capture(customer.page, 'customer-profile.png')
    const security = customer.page.locator('.profile-security form').nth(1)
    await security.locator('[name=currentPassword]').fill('Private-QA-2026!'); await security.locator('[name=newPassword]').fill('Changed-Private-2026!'); await security.locator('[name=confirmPassword]').fill('Changed-Private-2026!')
    await security.getByRole('button', { name: 'Actualizar contraseña', exact: true }).click(); await customer.page.getByRole('button', { name: 'Ingresar', exact: true }).first().waitFor()
    check('Password change revokes prior access', (await response(customer, '/users/me')).status === 401)
    const relogged = await login(customer.email, 'Changed-Private-2026!'); check('New password authenticates after persisted change', relogged.userId === customer.userId)
    // Administrative creation must be atomic, rather than three independent requests.
    await admin.page.getByRole('button', { name: 'Proveedores', exact: true }).click()
    const providerForm = admin.page.locator('.operations-form form'), email = 'qa.managed.' + nonce + '@example.test'
    await providerForm.locator('[name=displayName]').fill('QA Managed Supplier'); await providerForm.locator('[name=email]').fill(email); await providerForm.locator('[name=password]').fill('Managed-Private-2026!')
    await providerForm.locator('[name=businessName]').fill('QA Managed Business ' + nonce); await providerForm.locator('[name=documentNumber]').fill('M-' + nonce)
    const managedWait = admin.page.waitForResponse(r => r.url().endsWith('/suppliers/accounts') && r.request().method() === 'POST')
    await providerForm.getByRole('button', { name: 'Crear proveedor', exact: true }).click(); const managed = await (await managedWait).json(); result.records.managedSupplierId = managed.userId
    check('Admin creates supplier account in one transaction', (await api(admin, '/suppliers')).some(row => row.userId === managed.userId))
    const providerRow = admin.page.locator('.dashboard-list article').filter({ hasText: email })
    await providerRow.getByRole('button', { name: 'Editar', exact: true }).click(); await providerForm.locator('[name=businessName]').fill('QA Managed Business Edited ' + nonce); const savedProvider = admin.page.waitForResponse(r => r.url().endsWith('/suppliers/' + managed.userId) && r.request().method() === 'PUT'); await providerForm.getByRole('button', { name: 'Guardar proveedor', exact: true }).click(); await savedProvider
    check('Admin supplier edit is readable', (await api(admin, '/suppliers')).some(row => row.userId === managed.userId && row.businessName === 'QA Managed Business Edited ' + nonce))
    await capture(admin.page, 'admin-suppliers.png')
    const managedActor = await login(email, 'Managed-Private-2026!')
    check('A different supplier cannot publish the owner tool', (await response(managedActor, '/tools/' + toolId + '/publication', 'PATCH', { published: false })).status === 403)
    await providerRow.getByRole('button', { name: 'Desactivar', exact: true }).click(); await providerRow.getByText('DISABLED', { exact: true }).waitFor()
    check('Supplier deactivation revokes its session', (await response(managedActor, '/tools')).status === 401)
    await admin.page.getByRole('button', { name: 'Administradores', exact: true }).click()
    const adminForm = admin.page.locator('.operations-form form'), adminEmail = 'qa.admin.' + nonce + '@example.test'
    await adminForm.locator('[name=displayName]').fill('QA Managed Administrator'); await adminForm.locator('[name=email]').fill(adminEmail); await adminForm.locator('[name=password]').fill('Managed-Private-2026!')
    const adminWait = admin.page.waitForResponse(r => r.url().endsWith('/users/administrators') && r.request().method() === 'POST')
    await adminForm.getByRole('button', { name: 'Crear administrador', exact: true }).click(); const createdAdmin = await (await adminWait).json(); result.records.managedAdminId = createdAdmin.userId
    const adminRow = admin.page.locator('.dashboard-list article').filter({ hasText: adminEmail }); await adminRow.getByRole('button', { name: 'Editar', exact: true }).click()
    await adminForm.locator('[name=displayName]').fill('QA Administrator Edited'); const savedAdministrator = admin.page.waitForResponse(r => r.url().endsWith('/users/' + createdAdmin.userId) && r.request().method() === 'PUT'); await adminForm.getByRole('button', { name: 'Guardar administrador', exact: true }).click(); await savedAdministrator
    check('Admin profile edit is persisted', (await api(admin, '/users/' + createdAdmin.userId)).displayName === 'QA Administrator Edited')
    await capture(admin.page, 'admin-accounts.png'); await adminRow.getByRole('button', { name: 'Desactivar', exact: true }).click(); await adminRow.getByText('DISABLED', { exact: true }).waitFor()
    check('Admin deactivation is persisted', (await api(admin, '/users/' + createdAdmin.userId)).accountStatus === 'DISABLED')
    const newSupplier = await register(true); result.records.selfSupplierId = newSupplier.userId
    check('Self registered supplier has immediate access', (await response(newSupplier, '/tools')).status === 200)
    await newSupplier.page.getByRole('button', { name: 'Herramientas', exact: true }).click(); await newSupplier.page.getByRole('heading', { name: 'No hay herramientas registradas', exact: true }).waitFor()
    check('New supplier owns no other supplier inventory', true)
    await capture(newSupplier.page, 'supplier-registration.png')
    await relogged.page.goto(base + '/catalogo'); await relogged.page.setViewportSize({ width: 390, height: 844 })
    await relogged.page.getByRole('textbox', { name: 'Buscar herramienta o categoría' }).fill(name)
    await relogged.page.getByRole('heading', { name: name + ' Edited', exact: true }).waitFor()
    check('Mobile public catalog reads the saved supplier tool', await relogged.page.evaluate(() => document.documentElement.scrollWidth <= innerWidth))
    await capture(relogged.page, 'mobile-persisted-catalog.png')
    check('No uncaught JavaScript errors', errors.length === 0)
    result.completed = true
  } catch (error) {
    result.completed = false; result.failures.push(error.message)
    for (const context of contexts) for (const tab of context.pages()) { const alerts = await tab.locator('main [role=alert]').allTextContents(); result.failures.push(...alerts) }
  } finally { for (const context of contexts) await context.close() }
  return result
}
