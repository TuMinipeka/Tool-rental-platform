import { mkdirSync, writeFileSync } from 'node:fs'

const workspace = 'wrk_tool_rental_system'
const resources = [
  { _id: workspace, _type: 'workspace', name: 'Tool Rental - System Acceptance', scope: 'collection', description: 'Critical system tests against the isolated backend on localhost:18080. Creates uniquely named fixtures; never run against the working or public database.' },
  { _id: 'env_system_base', _type: 'environment', parentId: workspace, name: 'Base environment', data: {
    base_url: 'http://localhost:18080', supplier_email: 'proveedor@toolrental.local', customer_email: 'cliente@toolrental.local',
    demo_password: 'ToolRental2026!', supplier_token: '', customer_token: '', category_id: '', tool_id: '', unit_id: '', reservation_id: '', payment_id: '', invoice_id: '',
  } },
]
const guard = `const target = new URL(insomnia.environment.get('base_url'));
if (!['localhost', '127.0.0.1'].includes(target.hostname) || target.port !== '18080') throw new Error('Use the isolated local backend on port 18080; these requests write test fixtures.');`
let sequence = 0
function request(name, method, path, { actor, body, status = 200, before = '', after = '', headers = [] } = {}) {
  const number = String(++sequence).padStart(2, '0')
  const bearer = actor ? [{ name: 'Authorization', value: actor === 'invalid' ? 'Bearer invalid.jwt.signature' : `Bearer {{ _.${actor}_token }}` }] : []
  resources.push({
    _id: 'req_system_' + number, _type: 'request', parentId: workspace, name: `${number} ${name}`, metaSortKey: sequence,
    method, url: '{{ _.base_url }}' + path, authentication: {}, parameters: [], settingSendCookies: false, settingStoreCookies: false,
    headers: [...bearer, ...(body ? [{ name: 'Content-Type', value: 'application/json' }] : []), ...headers],
    body: body ? { mimeType: 'application/json', text: JSON.stringify(body, null, 2) } : {},
    preRequestScript: guard + '\n' + before,
    afterResponseScript: `insomnia.test('${number} ${name}: HTTP ${status}', () => insomnia.expect(insomnia.response.code).to.eql(${status}));\nlet json; try { json = insomnia.response.json(); } catch {}\n` + after,
  })
}
const test = (label, assertion) => `insomnia.test(${JSON.stringify(label)}, () => { ${assertion} });`
const id = (field, expression) => `insomnia.environment.set('${field}', ${expression});`
const saveAuthentication = actor => test(actor + ' JWT and role', `insomnia.expect(json.accessToken).to.be.a('string'); insomnia.expect(json.roles).to.include('${actor === 'supplier' ? 'SUPPLIER' : 'CUSTOMER'}');`)
  + id(actor + '_token', 'json.accessToken') + `const refreshCookie=insomnia.response.headers.find(h=>h.key.toLowerCase()==='set-cookie' && h.value.startsWith('trp_refresh=')); if(refreshCookie) insomnia.environment.set('${actor}_refresh',refreshCookie.value.split(';')[0]);`
const paymentBody = reservation => ({ reservationId: '{{ _.' + reservation + ' }}', paymentMethod: 'CREDIT_CARD', cardNetwork: 'VISA', cardLastFour: '1111', cardExpiryMonth: 12, cardExpiryYear: 2030 })

request('Public features and initialize fixture identities', 'GET', '/api/v1/public/features', {
  before: `const run=Date.now().toString(); insomnia.environment.set('run_id',run); insomnia.environment.set('tool_name','INSO Drill '+run); const today=new Intl.DateTimeFormat('en-CA',{timeZone:'America/Bogota'}).format(new Date()); insomnia.environment.set('rental_date',today); const next=new Date(Date.now()+86400000); insomnia.environment.set('decline_date',new Intl.DateTimeFormat('en-CA',{timeZone:'America/Bogota'}).format(next));`,
  after: test('Fictional provider is enabled', "insomnia.expect(json.paymentProvider).to.eql('FICTIONAL_GATEWAY');"),
})
request('Protected route without JWT', 'GET', '/api/v1/tools', { status: 401, after: test('Stable authentication problem', "insomnia.expect(json.code).to.eql('AUTHENTICATION_REQUIRED'); insomnia.expect(json.traceId).to.be.a('string');") })
request('Protected route with invalid JWT', 'GET', '/api/v1/tools', { actor: 'invalid', status: 401 })
request('Public catalog and category reference', 'GET', '/api/v1/public/catalog', { after: test('Catalog uses published server data', 'insomnia.expect(json.length).to.be.above(0);') + id('category_id', 'json[0].categoryId') })
request('Login supplier', 'POST', '/api/v1/auth/login', { body: { email: '{{ _.supplier_email }}', password: '{{ _.demo_password }}' }, after: saveAuthentication('supplier') })
request('POST supplier tool', 'POST', '/api/v1/tools', { actor: 'supplier', status: 201, body: { categoryId: '{{ _.category_id }}', name: '{{ _.tool_name }}', description: 'Created by the Insomnia system acceptance collection', serviceCity: 'Bucaramanga', dailyRate: 25000, currency: 'COP', imageUrls: [], published: true }, after: id('tool_id', 'json.id.value') + test('Tool has its persisted identity', 'insomnia.expect(json.id.value).to.be.a("string");') })
request('PUT supplier tool', 'PUT', '/api/v1/tools/{{ _.tool_id }}', { actor: 'supplier', body: { name: '{{ _.tool_name }} Updated', description: 'Updated through PUT in Insomnia', serviceCity: 'Bucaramanga', dailyRate: 25000, currency: 'COP', published: true }, after: test('PUT returns updated fields', "insomnia.expect(json.name).to.include('Updated');") })
request('GET persisted supplier tool', 'GET', '/api/v1/tools/{{ _.tool_id }}', { actor: 'supplier', after: test('Fresh read preserves PUT', "insomnia.expect(json.name).to.include('Updated'); insomnia.expect(json.dailyRate.amount).to.eql(25000); insomnia.expect(json.dailyRate.currency).to.eql('COP');") })
request('DELETE withdraws publication', 'DELETE', '/api/v1/tools/{{ _.tool_id }}', { actor: 'supplier', status: 204 })
request('Withdrawn tool is absent from public catalog', 'GET', '/api/v1/public/catalog/{{ _.tool_id }}', { status: 404 })
request('Withdrawal preserves private tool history', 'GET', '/api/v1/tools/{{ _.tool_id }}', { actor: 'supplier', after: test('DELETE is a logical withdrawal', 'insomnia.expect(json.published).to.eql(false);') })
request('Republish the tool', 'PATCH', '/api/v1/tools/{{ _.tool_id }}/publication', { actor: 'supplier', body: { published: true } })
request('Register a physical equipment unit', 'POST', '/api/v1/equipment-units', { actor: 'supplier', status: 201, body: { toolId: '{{ _.tool_id }}', inventoryCode: 'INSO-{{ _.run_id }}', serialNumber: 'INSO-SERIAL-{{ _.run_id }}', currentCondition: 'GOOD', status: 'OPERATIONAL' }, after: id('unit_id', 'json.id.value') })
request('Login customer', 'POST', '/api/v1/auth/login', { body: { email: '{{ _.customer_email }}', password: '{{ _.demo_password }}' }, after: saveAuthentication('customer') })
request('Customer cannot access administrator users', 'GET', '/api/v1/users', { actor: 'customer', status: 403 })
request('Customer cannot modify supplier publication', 'PUT', '/api/v1/tools/{{ _.tool_id }}', { actor: 'customer', status: 403, body: { name: 'Forbidden modification', description: 'Must be rejected', serviceCity: 'Bucaramanga', dailyRate: 1, currency: 'COP', published: true } })
request('Create held reservation', 'POST', '/api/v1/reservations', {
  actor: 'customer', status: 201,
  before: "insomnia.environment.set('scheduled_at',new Date(Date.now()+1000).toISOString());",
  body: { equipmentUnitIds: ['{{ _.unit_id }}'], startDate: '{{ _.rental_date }}', returnDate: '{{ _.rental_date }}', fulfillmentMethod: 'CUSTOMER_PICKUP', scheduledFulfillmentAt: '{{ _.scheduled_at }}' },
  after: id('reservation_id', 'json.id.value') + test('Server price and inclusive one-day period', "insomnia.expect(json.status).to.eql('PENDING_PAYMENT'); insomnia.expect(json.total.amount).to.eql(25000);"),
})
request('A second overlapping reservation is rejected', 'POST', '/api/v1/reservations', { actor: 'customer', status: 409, body: { equipmentUnitIds: ['{{ _.unit_id }}'], startDate: '{{ _.rental_date }}', returnDate: '{{ _.rental_date }}', fulfillmentMethod: 'CUSTOMER_PICKUP', scheduledFulfillmentAt: '{{ _.scheduled_at }}' } })
request('Create fictional checkout', 'POST', '/api/v1/payments/checkouts', { actor: 'customer', headers: [{ name: 'Idempotency-Key', value: 'inso-approved-{{ _.run_id }}' }], body: paymentBody('reservation_id'), after: id('payment_id', 'json.paymentId') + test('Checkout amount comes from reservation', 'insomnia.expect(json.amount).to.eql(25000);') })
request('Checkout retry reuses the same payment', 'POST', '/api/v1/payments/checkouts', { actor: 'customer', headers: [{ name: 'Idempotency-Key', value: 'inso-approved-{{ _.run_id }}' }], body: paymentBody('reservation_id'), after: test('Checkout is idempotent', "insomnia.expect(json.paymentId).to.eql(insomnia.environment.get('payment_id'));") })
request('Approve fictional payment', 'POST', '/api/v1/payments/fictional/checkouts/{{ _.payment_id }}/decision', { actor: 'customer', body: { outcome: 'APPROVED' } })
request('Duplicate approval is idempotent', 'POST', '/api/v1/payments/fictional/checkouts/{{ _.payment_id }}/decision', { actor: 'customer', body: { outcome: 'APPROVED' } })
request('Payment confirms the reservation', 'GET', '/api/v1/reservations/{{ _.reservation_id }}', { actor: 'customer', after: test('Reservation is confirmed', "insomnia.expect(json.status).to.eql('CONFIRMED');") })
request('Exactly one academic receipt is issued', 'GET', '/api/v1/invoices', { actor: 'customer', after: `const invoices=json.filter(row=>row.reservationId===insomnia.environment.get('reservation_id'));` + test('One receipt despite duplicate approval', 'insomnia.expect(invoices.length).to.eql(1);') + id('invoice_id', 'invoices[0].invoiceId') })
request('Download academic receipt PDF', 'GET', '/api/v1/invoices/{{ _.invoice_id }}/document?disposition=attachment', { actor: 'customer', after: test('Receipt is a PDF', "insomnia.expect(insomnia.response.headers.find(h=>h.key.toLowerCase()==='content-type').value).to.include('application/pdf');") })
request('Schedule supplier delivery or pickup', 'POST', '/api/v1/unit-deliveries', { actor: 'supplier', status: 201, body: { reservationId: '{{ _.reservation_id }}', equipmentUnitId: '{{ _.unit_id }}', scheduledAt: '{{ _.scheduled_at }}', outboundCondition: 'GOOD', addressLine: 'Academic pickup point', city: 'Bucaramanga', region: 'Santander', country: 'CO' } })
request('Complete physical delivery', 'PATCH', '/api/v1/unit-deliveries/entry/completion?reservationId={{ _.reservation_id }}&equipmentUnitId={{ _.unit_id }}', { actor: 'supplier', before: "const wait=new Date(insomnia.environment.get('scheduled_at')).getTime()-Date.now()+100; if(wait>0) await new Promise(resolve=>setTimeout(resolve,wait));", body: { condition: 'GOOD' } })
request('Start rental', 'POST', '/api/v1/rentals', { actor: 'supplier', status: 201, body: { reservationId: '{{ _.reservation_id }}' }, after: test('Rental is active', "insomnia.expect(json.rentalStatus).to.eql('ACTIVE');") })
request('Register equipment return', 'POST', '/api/v1/unit-returns', { actor: 'supplier', status: 201, body: { reservationId: '{{ _.reservation_id }}', equipmentUnitId: '{{ _.unit_id }}', inboundCondition: 'GOOD', notes: 'Returned during the Insomnia acceptance flow' } })
request('Complete rental', 'PATCH', '/api/v1/rentals/{{ _.reservation_id }}/status', { actor: 'supplier', body: { status: 'COMPLETED' } })
request('GET completed rental', 'GET', '/api/v1/rentals/{{ _.reservation_id }}', { actor: 'supplier', after: test('Full operational lifecycle persisted', "insomnia.expect(json.rentalStatus).to.eql('COMPLETED');") })
request('Create reservation for declined payment', 'POST', '/api/v1/reservations', { actor: 'customer', status: 201, body: { equipmentUnitIds: ['{{ _.unit_id }}'], startDate: '{{ _.decline_date }}', returnDate: '{{ _.decline_date }}', fulfillmentMethod: 'CUSTOMER_PICKUP', scheduledFulfillmentAt: '{{ _.decline_date }}T15:00:00Z' }, after: id('decline_reservation_id', 'json.id.value') })
request('Create checkout for decline scenario', 'POST', '/api/v1/payments/checkouts', { actor: 'customer', headers: [{ name: 'Idempotency-Key', value: 'inso-declined-{{ _.run_id }}' }], body: paymentBody('decline_reservation_id'), after: id('decline_payment_id', 'json.paymentId') })
request('Decline fictional payment', 'POST', '/api/v1/payments/fictional/checkouts/{{ _.decline_payment_id }}/decision', { actor: 'customer', body: { outcome: 'DECLINED' } })
request('Declined payment releases the reservation', 'GET', '/api/v1/reservations/{{ _.decline_reservation_id }}', { actor: 'customer', after: test('Reservation payment failed', "insomnia.expect(json.status).to.eql('PAYMENT_FAILED');") })
request('Declined payment releases availability', 'GET', '/api/v1/public/catalog/{{ _.tool_id }}/availability?startDate={{ _.decline_date }}&returnDate={{ _.decline_date }}', { after: test('Unit becomes available after decline', "insomnia.expect(json.availableEquipmentUnitIds).to.include(insomnia.environment.get('unit_id'));") })
request('Initialize CSRF cookie and header token', 'GET', '/api/v1/auth/csrf', { after: id('csrf_token', 'json.token') + id('csrf_cookie', "insomnia.response.headers.find(h=>h.key.toLowerCase()==='set-cookie' && h.value.startsWith('XSRF-TOKEN=')).value.split(';')[0]") })
request('Refresh without CSRF is rejected', 'POST', '/api/v1/auth/refresh', { status: 403, headers: [{ name: 'Cookie', value: '{{ _.customer_refresh }}' }] })
request('Rotate refresh and receive a new access token', 'POST', '/api/v1/auth/refresh', { headers: [{ name: 'X-XSRF-TOKEN', value: '{{ _.csrf_token }}' }, { name: 'Cookie', value: '{{ _.customer_refresh }}; {{ _.csrf_cookie }}' }], after: saveAuthentication('customer') })
request('Logout revokes the customer session', 'POST', '/api/v1/auth/logout', { status: 204, headers: [{ name: 'X-XSRF-TOKEN', value: '{{ _.csrf_token }}' }, { name: 'Cookie', value: '{{ _.customer_refresh }}; {{ _.csrf_cookie }}' }] })
request('Revoked access token no longer authenticates', 'GET', '/api/v1/users/me', { actor: 'customer', status: 401 })

mkdirSync('docs/insomnia', { recursive: true })
writeFileSync('docs/insomnia/system-tests.json', JSON.stringify({ _type: 'export', __export_format: 4, __export_date: new Date().toISOString(), __export_source: 'tool-rental-system-tests', resources }, null, 2) + '\n')
console.log(`Generated ${sequence} Insomnia requests with response assertions. No live tokens were exported.`)
