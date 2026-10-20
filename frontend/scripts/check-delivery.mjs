import assert from 'node:assert/strict'

const base = process.env.AUDIT_URL || 'http://localhost:4173'
const document = await fetch(base)
assert.equal(document.status, 200)
assert.match(document.headers.get('cache-control') || '', /no-cache/)
const html = await document.text()
const asset = html.match(/src="(\/assets\/[^"?]+\.js)"/)[1]
const script = await fetch(`${base}${asset}`, { headers: { 'Accept-Encoding': 'gzip' } })
assert.equal(script.status, 200)
assert.equal(script.headers.get('content-encoding'), 'gzip')
assert.match(script.headers.get('cache-control') || '', /max-age=31536000, immutable/)
assert.equal((await fetch(`${base}/assets/missing-qa.js`)).status, 404)
assert.equal((await fetch(`${base}/catalogo`)).status, 200)
assert.equal((await fetch(`${base}/robots.txt`)).status, 200)
console.log('Nginx verified: gzip, immutable versioned assets, revalidated HTML, SPA routes, missing asset 404, robots.txt')
