const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '')
const sessionKey = 'tool-rental-session'

export class ApiError extends Error {
  constructor(message, status, details) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.details = details
  }
}

export function readSession() {
  try {
    return JSON.parse(window.sessionStorage.getItem(sessionKey))
  } catch {
    return null
  }
}

export function saveSession(session) {
  if (session) window.sessionStorage.setItem(sessionKey, JSON.stringify(session))
  else window.sessionStorage.removeItem(sessionKey)
}

async function request(path, options = {}) {
  const session = readSession()
  const headers = new Headers(options.headers)
  if (options.body) headers.set('Content-Type', 'application/json')
  if (session?.accessToken) headers.set('Authorization', `Bearer ${session.accessToken}`)

  let response
  try {
    response = await fetch(`${apiBaseUrl}${path}`, { ...options, headers })
  } catch (cause) {
    throw new ApiError('No fue posible conectar con el servicio. Revisa que el backend esté activo.', 0, cause)
  }

  const contentType = response.headers.get('content-type') || ''
  const payload = contentType.includes('application/json') ? await response.json() : null
  if (!response.ok) {
    const message = payload?.message || payload?.detail || `La operación no pudo completarse (${response.status}).`
    throw new ApiError(message, response.status, payload)
  }
  return payload
}

export const api = {
  listCatalog(params = {}) {
    const query = new URLSearchParams()
    if (params.query) query.set('query', params.query)
    if (params.categoryId) query.set('categoryId', params.categoryId)
    return request(`/api/public/catalog${query.size ? `?${query}` : ''}`)
  },
  getCatalogTool(toolId) {
    return request(`/api/public/catalog/${toolId}`)
  },
  getAvailability(toolId, startDate, returnDate) {
    const query = new URLSearchParams({ startDate, returnDate })
    return request(`/api/public/catalog/${toolId}/availability?${query}`)
  },
  login(email, password) {
    return request('/api/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) })
  },
  register(account) {
    return request('/api/auth/register', { method: 'POST', body: JSON.stringify(account) })
  },
  recoverPassword(email) {
    return request('/api/auth/password-recovery', { method: 'POST', body: JSON.stringify({ email }) })
  },
  resetPassword(token, newPassword) {
    return request('/api/auth/password-reset', { method: 'POST', body: JSON.stringify({ token, newPassword }) })
  },
  logout() {
    return request('/api/auth/logout', { method: 'POST' })
  },
  quoteReservation(startDate, returnDate, equipmentUnitIds) {
    return request('/api/quotes/reservations', {
      method: 'POST',
      body: JSON.stringify({ startDate, returnDate, equipmentUnitIds }),
    })
  },
  createReservation(startDate, returnDate, equipmentUnitIds) {
    return request('/api/reservations', {
      method: 'POST',
      body: JSON.stringify({ startDate, returnDate, equipmentUnitIds }),
    })
  },
  listReservations() {
    return request('/api/reservations')
  },
  simulatePayment(payment) {
    return request('/api/payments/simulations', {
      method: 'POST',
      body: JSON.stringify(payment),
    })
  },
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
    location: 'Bogotá, Colombia',
    rating: null,
    reviews: null,
    stock: tool.operationalUnitCount,
    color: colors[index % colors.length],
    accent: tool.operationalUnitCount > 0 ? 'Disponible' : 'Sin existencias',
    description: tool.description,
    imageUrl: tool.imageUrls?.[0] || null,
    source: 'api',
  }
}
