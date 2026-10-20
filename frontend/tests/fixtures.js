import { demoCatalog } from '../src/demoCatalog.js'

export const catalog = demoCatalog.map(tool => ({
  toolId: tool.id, supplierId: '30000000-0000-0000-0000-000000000001',
  categoryId: tool.category, categoryName: tool.category, name: tool.name,
  description: tool.description, dailyRate: tool.price, currency: 'COP',
  serviceCity: tool.location, operationalUnitCount: tool.stock, imageUrls: [],
}))

export async function mockApi(page, { empty = false, unavailable = false, roles = ['CUSTOMER'] } = {}) {
  await page.route('**/api/v1/**', async route => {
    const url = new URL(route.request().url())
    const path = url.pathname.replace('/api/v1', '')
    let status = 200
    let body = []
    if (path === '/public/catalog') {
      status = unavailable ? 503 : 200
      body = unavailable ? { detail: 'Catalog temporarily unavailable' } : empty ? [] : catalog
    } else if (path.endsWith('/availability')) body = { availableEquipmentUnitIds: ['40000000-0000-0000-0000-000000000001'] }
    else if (path === '/auth/csrf') body = { headerName: 'X-XSRF-TOKEN', token: 'qa-csrf-token' }
    else if (path === '/auth/refresh') { status = 401; body = { code: 'SESSION_REQUIRED', detail: 'No active session' } }
    else if (path === '/auth/login' || path === '/auth/register' || path === '/auth/supplier-registration') body = {
      accessToken: 'qa-access-token', userId: 'qa-customer', email: 'qa@example.test',
      displayName: 'QA Customer', roles: path === '/auth/supplier-registration' ? ['SUPPLIER'] : roles,
    }
    else if (path === '/public/features') body = { hostedCheckout: true, paymentProvider: 'MERCADO_PAGO', currency: 'COP' }
    else if (path === '/quotes/reservations') body = { total: 80000, currency: 'COP' }
    else if (path === '/reservations' && route.request().method() === 'POST') body = { id: 'qa-reservation' }
    else if (path === '/auth/sessions') body = []
    await route.fulfill({ status, contentType: 'application/json', body: JSON.stringify(body) })
  })
}
