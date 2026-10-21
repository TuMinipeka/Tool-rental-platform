import { notifyCatalogChange } from './lib/catalogChanges.js'

const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '')
const apiPrefix = '/api/v1'
const demoMode = import.meta.env.VITE_DEMO_MODE === 'true'

let activeSession = null
let refreshPromise = null

export class ApiError extends Error {
  constructor(message, status, details) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = details?.code || null
    this.details = details
  }
}

export function readSession() {
  return activeSession
}

export function saveSession(session) {
  activeSession = session || null
  if (typeof window !== 'undefined') {
    window.dispatchEvent(new CustomEvent('tool-rental-session', { detail: activeSession }))
  }
}

async function parseResponse(response) {
  const contentType = response.headers.get('content-type') || ''
  if (!contentType.includes('application/json') && !contentType.includes('application/problem+json')) return null
  return response.json()
}

function toApiError(response, payload) {
  const message =
    payload?.message ||
    payload?.detail ||
    `La operaciÃ³n no pudo completarse (${response.status}).`
  return new ApiError(message, response.status, payload)
}

async function fetchApi(path, options = {}) {
  try {
    return await fetch(`${apiBaseUrl}${path}`, {
      ...options,
      credentials: 'include',
    })
  } catch (cause) {
    throw new ApiError(
      'No fue posible conectar con el servicio. Revisa que el backend estÃ© activo.',
      0,
      cause,
    )
  }
}

async function getCsrfToken() {
  const response = await fetchApi(`${apiPrefix}/auth/csrf`)
  const payload = await parseResponse(response)
  if (!response.ok) throw toApiError(response, payload)

  return {
    headerName: payload.headerName,
    token: payload.token,
  }
}

async function refreshSession() {
  if (refreshPromise) return refreshPromise

  refreshPromise = (async () => {
    const csrf = await getCsrfToken()
    const response = await fetchApi(`${apiPrefix}/auth/refresh`, {
      method: 'POST',
      headers: { [csrf.headerName]: csrf.token },
    })
    const payload = await parseResponse(response)
    if (!response.ok) {
      saveSession(null)
      throw toApiError(response, payload)
    }
    saveSession(payload)
    return payload
  })().finally(() => {
    refreshPromise = null
  })

  return refreshPromise
}

async function request(path, options = {}, behavior = {}) {
  const { authenticated = false, csrf = false, retry = true } = behavior
  const headers = new Headers(options.headers)
  if (typeof options.body === 'string') headers.set('Content-Type', 'application/json')
  if (authenticated && activeSession?.accessToken) {
    headers.set('Authorization', `Bearer ${activeSession.accessToken}`)
  }
  if (csrf) {
    const currentCsrf = await getCsrfToken()
    headers.set(currentCsrf.headerName, currentCsrf.token)
  }

  const response = await fetchApi(`${apiPrefix}${path}`, { ...options, headers })
  const payload = behavior.responseType === 'text' && response.ok
    ? await response.text()
    : behavior.responseType === 'blob' && response.ok
      ? await response.blob()
      : await parseResponse(response)

  if (response.status === 401 && authenticated && retry) {
    try {
      await refreshSession()
    } catch {
      throw toApiError(response, payload)
    }
    return request(path, options, { ...behavior, retry: false })
  }

  if (!response.ok) throw toApiError(response, payload)
  if (options.method && options.method !== 'GET' && /^(\/tools|\/tool-images|\/equipment-units|\/categories)(\/|\?|$)/.test(path)) notifyCatalogChange()
  return payload
}

export const api = {
  restoreSession() {
    return refreshSession()
  },
  listCatalog(params = {}) {
    const query = new URLSearchParams()
    if (params.query) query.set('query', params.query)
    if (params.categoryId) query.set('categoryId', params.categoryId)
    if (params.serviceCity) query.set('serviceCity', params.serviceCity)
    return request(`/public/catalog${query.size ? `?${query}` : ''}`, { cache: 'no-store' })
  },
  getCatalogTool(toolId) {
    return request(`/public/catalog/${toolId}`)
  },
  getAvailability(toolId, startDate, returnDate) {
    const query = new URLSearchParams({ startDate, returnDate })
    return request(`/public/catalog/${toolId}/availability?${query}`, { cache: 'no-store' })
  },
  login(email, password) {
    return request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    })
  },
  registerSupplier(account) { return request('/auth/supplier-registration', { method: 'POST', body: JSON.stringify(account) }) },
  updateUserStatus(id, status) { return request('/users/' + encodeURIComponent(id) + '/status', { method: 'PATCH', body: JSON.stringify({ status }) }, { authenticated: true }) },
  updateUser(id, profile) { return request('/users/' + encodeURIComponent(id), { method: 'PUT', body: JSON.stringify(profile) }, { authenticated: true }) },
  createManagedSupplier(account) { return request('/suppliers/accounts', { method: 'POST', body: JSON.stringify(account) }, { authenticated: true }) },
  createAdministrator(account) { return request('/users/administrators', { method: 'POST', body: JSON.stringify(account) }, { authenticated: true }) },
  createUser(user) { return request('/users', { method: 'POST', body: JSON.stringify(user) }, { authenticated: true }) },
  currentUser() { return request('/users/me', {}, { authenticated: true }) },
  updateCurrentUser(profile) { return request('/users/me/profile', { method: 'PATCH', body: JSON.stringify(profile) }, { authenticated: true }) },
  changePassword(passwords) { return request('/users/me/password', { method: 'PATCH', body: JSON.stringify(passwords) }, { authenticated: true }) },
  register(account) {
    return request('/auth/register', { method: 'POST', body: JSON.stringify(account) })
  },
  async logout() {
    try {
      return await request('/auth/logout', { method: 'POST' }, { csrf: true })
    } finally {
      saveSession(null)
    }
  },
  quoteReservation(quote) {
    return request(
      '/quotes/reservations',
      { method: 'POST', body: JSON.stringify(quote) },
      { authenticated: true },
    )
  },
  createReservation(reservation) {
    return request(
      '/reservations',
      { method: 'POST', body: JSON.stringify(reservation) },
      { authenticated: true },
    )
  },
  getReservation(id) {
    return request('/reservations/' + encodeURIComponent(id), {}, { authenticated: true })
  },
  reservationAction(id, action, idempotencyKey) {
    return request('/reservations/' + encodeURIComponent(id) + '/' + action,
      { method: 'POST', headers: idempotencyKey ? { 'Idempotency-Key': idempotencyKey } : {} },
      { authenticated: true })
  },
  requestReservationExtension(id, requestedReturnDate, idempotencyKey) { return request('/reservation-extensions/reservations/' + encodeURIComponent(id), { method: 'POST', headers: { 'Idempotency-Key': idempotencyKey }, body: JSON.stringify({ requestedReturnDate }) }, { authenticated: true }) },
  features() { return request('/public/features', {}) },
  decideFictionalPayment(id, outcome) { return request('/payments/fictional/checkouts/' + encodeURIComponent(id) + '/decision', { method: 'POST', body: JSON.stringify({ outcome }) }, { authenticated: true }) },
  checkout(id) { return request('/payments/checkouts/' + encodeURIComponent(id), {}, { authenticated: true }) },
  createCheckout(reservationId, idempotencyKey, paymentMethod) {
    return request('/payments/checkouts', { method: 'POST', headers: { 'Idempotency-Key': idempotencyKey }, body: JSON.stringify({ reservationId, ...paymentMethod }) }, { authenticated: true })
  },
  invoiceDocument(id, disposition = 'inline') { return request('/invoices/' + encodeURIComponent(id) + '/document?' + new URLSearchParams({ disposition }), {}, { authenticated: true, responseType: 'blob' }) },
  operationalReport(kind, from, to) { return request('/reports/' + kind + '?' + new URLSearchParams({ from, to }), {}, { authenticated: true }) },
  deliveryEntry(reservationId, equipmentUnitId) { return request('/unit-deliveries/entry?' + new URLSearchParams({ reservationId, equipmentUnitId }), {}, { authenticated: true }) },
  returnEntry(reservationId, equipmentUnitId) { return request('/unit-returns/entry?' + new URLSearchParams({ reservationId, equipmentUnitId }), {}, { authenticated: true }) },
  getRental(id) { return request('/rentals/' + encodeURIComponent(id), {}, { authenticated: true }) },
  scheduleDelivery(body) { return request('/unit-deliveries', { method: 'POST', body: JSON.stringify(body) }, { authenticated: true }) },
  completeDelivery(reservationId, equipmentUnitId, condition) { return request('/unit-deliveries/entry/completion?' + new URLSearchParams({ reservationId, equipmentUnitId }), { method: 'PATCH', body: JSON.stringify({ condition }) }, { authenticated: true }) },
  startRental(reservationId) { return request('/rentals', { method: 'POST', body: JSON.stringify({ reservationId }) }, { authenticated: true }) },
  returnUnit(body) { return request('/unit-returns', { method: 'POST', body: JSON.stringify({...body,notes:body.notes?.trim()||null}) }, { authenticated: true }) },
  completeRental(id) { return request('/rentals/' + encodeURIComponent(id) + '/status', { method: 'PATCH', body: JSON.stringify({ status: 'COMPLETED' }) }, { authenticated: true }) },
  listRentals() { return request('/rentals', {}, { authenticated: true }) },
  listPayments() { return request('/payments', {}, { authenticated: true }) },
  listInvoices() { return request('/invoices', {}, { authenticated: true }) },
  listReservations() {
    return request('/reservations', {}, { authenticated: true })
  },
  listTools() {
    return request('/tools', {}, { authenticated: true })
  },
  createTool(tool) {
    return request(
      '/tools',
      { method: 'POST', body: JSON.stringify({ serviceCity: 'Bucaramanga', ...tool }) },
      { authenticated: true },
    )
  },
  updateTool(id, tool) { return request('/tools/' + encodeURIComponent(id), { method: 'PUT', body: JSON.stringify(tool) }, { authenticated: true }) },
  publishTool(id, published) { return request('/tools/' + encodeURIComponent(id) + '/publication', { method: 'PATCH', body: JSON.stringify({ published }) }, { authenticated: true }) },
  getTool(id) { return request('/tools/' + encodeURIComponent(id), {}, { authenticated: true }) },
  listToolImages(id) { return request('/tools/' + encodeURIComponent(id) + '/images', {}, { authenticated: true }) },
  addToolImage(toolId, imageUrl) { return request('/tool-images', { method: 'POST', body: JSON.stringify({ toolId, imageUrl }) }, { authenticated: true }) },
  uploadToolImage(id, file) { const body = new FormData(); body.append('file', file); return request('/tools/' + encodeURIComponent(id) + '/images', { method: 'POST', body }, { authenticated: true }) },
  removeToolImage(toolId, imageUrl) { return request('/tool-images/entry?' + new URLSearchParams({ toolId, imageUrl }), { method: 'DELETE' }, { authenticated: true }) },
  updateEquipmentUnit(id, unit) { return request('/equipment-units/' + encodeURIComponent(id), { method: 'PUT', body: JSON.stringify(unit) }, { authenticated: true }) },
  deleteTool(id) { return request('/tools/' + encodeURIComponent(id), { method: 'DELETE' }, { authenticated: true }) },
  listEquipmentUnits() {
    return request('/equipment-units', {}, { authenticated: true })
  },
  createEquipmentUnit(unit) {
    return request(
      '/equipment-units',
      { method: 'POST', body: JSON.stringify(unit) },
      { authenticated: true },
    )
  },
  listCategories() {
    return request('/categories', {}, { authenticated: true })
  },
  listUsers() {
    return request('/users', {}, { authenticated: true })
  },
  listSuppliers() { return request('/suppliers', {}, { authenticated: true }) },
  createSupplier(supplier) { return request('/suppliers', { method: 'POST', body: JSON.stringify(supplier) }, { authenticated: true }) },
  updateSupplier(id, supplier) { return request('/suppliers/' + encodeURIComponent(id), { method: 'PUT', body: JSON.stringify(supplier) }, { authenticated: true }) },
  deleteSupplier(id) { return request('/suppliers/' + encodeURIComponent(id), { method: 'DELETE' }, { authenticated: true }) },
  listRoles() { return request('/roles', {}, { authenticated: true }) },
  listUserRoles() { return request('/user-roles', {}, { authenticated: true }) },
  assignUserRole(userId, roleId) { return request('/user-roles', { method: 'POST', body: JSON.stringify({ userId, roleId }) }, { authenticated: true }) },
  simulatePayment(payment) {
    return request(
      '/payments/simulations',
      { method: 'POST', body: JSON.stringify(payment) },
      { authenticated: true },
    )
  },
}

export function resolveApiImageUrl(value) {
  return value?.startsWith('/api/v1/public/tool-images/files/') ? apiBaseUrl + value : value
}

export function normalizeCatalogTool(tool, index = 0) {
  const colors = ['orange', 'yellow', 'blue', 'green', 'red', 'silver']
  return {
    id: tool.toolId,
    supplierId: tool.supplierId,
    categoryId: tool.categoryId,
    name: tool.name,
    category: tool.categoryName,
    price: Number(tool.dailyRate),
    deposit: 0,
    location: tool.serviceCity,
    rating: null,
    reviews: null,
    stock: tool.operationalUnitCount,
    color: colors[index % colors.length],
    accent: demoMode
      ? 'DemostraciÃ³n'
      : tool.operationalUnitCount > 0
        ? 'CatÃ¡logo real'
        : 'Sin existencias',
    description: tool.description,
    imageUrl: resolveApiImageUrl(tool.imageUrls?.[0]) || null,
    source: 'api',
  }
}
