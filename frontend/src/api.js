const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '')
const apiPrefix = '/api/v1'

let activeSession = null
let csrfToken = null
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
  if (!contentType.includes('application/json')) return null
  return response.json()
}

function toApiError(response, payload) {
  const message =
    payload?.message ||
    payload?.detail ||
    `La operación no pudo completarse (${response.status}).`
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
      'No fue posible conectar con el servicio. Revisa que el backend esté activo.',
      0,
      cause,
    )
  }
}

async function getCsrfToken() {
  if (csrfToken) return csrfToken

  const response = await fetchApi(`${apiPrefix}/auth/csrf`)
  const payload = await parseResponse(response)
  if (!response.ok) throw toApiError(response, payload)

  csrfToken = {
    headerName: payload.headerName,
    token: payload.token,
  }
  return csrfToken
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
  if (options.body) headers.set('Content-Type', 'application/json')
  if (authenticated && activeSession?.accessToken) {
    headers.set('Authorization', `Bearer ${activeSession.accessToken}`)
  }
  if (csrf) {
    const currentCsrf = await getCsrfToken()
    headers.set(currentCsrf.headerName, currentCsrf.token)
  }

  const response = await fetchApi(`${apiPrefix}${path}`, { ...options, headers })
  const payload = await parseResponse(response)

  if (response.status === 401 && authenticated && retry) {
    try {
      await refreshSession()
    } catch {
      throw toApiError(response, payload)
    }
    return request(path, options, { ...behavior, retry: false })
  }

  if (!response.ok) throw toApiError(response, payload)
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
    return request(`/public/catalog${query.size ? `?${query}` : ''}`)
  },
  getCatalogTool(toolId) {
    return request(`/public/catalog/${toolId}`)
  },
  getAvailability(toolId, startDate, returnDate) {
    const query = new URLSearchParams({ startDate, returnDate })
    return request(`/public/catalog/${toolId}/availability?${query}`)
  },
  login(email, password) {
    return request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    })
  },
  register(account) {
    return request('/auth/register', { method: 'POST', body: JSON.stringify(account) })
  },
  recoverPassword(email) {
    return request('/auth/password-recovery', {
      method: 'POST',
      body: JSON.stringify({ email }),
    })
  },
  resetPassword(token, newPassword) {
    return request('/auth/password-reset', {
      method: 'POST',
      body: JSON.stringify({ token, newPassword }),
    })
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
  listReservations() {
    return request('/reservations', {}, { authenticated: true })
  },
  listTools() {
    return request('/tools', {}, { authenticated: true })
  },
  createTool(tool) {
    return request(
      '/tools',
      { method: 'POST', body: JSON.stringify(tool) },
      { authenticated: true },
    )
  },
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
  simulatePayment(payment) {
    return request(
      '/payments/simulations',
      { method: 'POST', body: JSON.stringify(payment) },
      { authenticated: true },
    )
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
    location: null,
    rating: null,
    reviews: null,
    stock: tool.operationalUnitCount,
    color: colors[index % colors.length],
    accent: tool.operationalUnitCount > 0 ? 'Catálogo real' : 'Sin existencias',
    description: tool.description,
    imageUrl: tool.imageUrls?.[0] || null,
    source: 'api',
  }
}
