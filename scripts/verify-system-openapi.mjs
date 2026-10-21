import { mkdirSync, writeFileSync } from 'node:fs'

const url = 'http://localhost:8080/v3/api-docs'
const response = await fetch(url)
if (!response.ok) throw new Error(`OpenAPI returned ${response.status}`)
const api = await response.json()
const operations = Object.entries(api.paths).flatMap(([path, item]) => Object.entries(item)
  .filter(([, operation]) => operation.operationId).map(([method, operation]) => ({ path, method, operation })))
let jsonRequests = 0
let jsonResponses = 0
const failures = []
function checkExample(schema, value, location) {
  if (!schema) return
  if (schema.$ref) return checkExample(api.components.schemas[schema.$ref.split('/').at(-1)], value, location)
  const types = Array.isArray(schema.type) ? schema.type : [schema.type]
  if (value === null && !types.includes('null')) return failures.push(`${location}: null example`)
  if (types.includes('array')) {
    if (!Array.isArray(value)) return failures.push(`${location}: expected array`)
    value.forEach(item => checkExample(schema.items, item, location + '[]'))
  } else if (schema.properties) {
    if (!value || typeof value !== 'object' || Array.isArray(value)) return failures.push(`${location}: expected object`)
    for (const [key, property] of Object.entries(schema.properties)) checkExample(property, value[key], `${location}.${key}`)
  } else if (types.includes('boolean') && typeof value !== 'boolean') failures.push(`${location}: expected boolean`)
  else if ((types.includes('number') || types.includes('integer')) && typeof value !== 'number') failures.push(`${location}: expected number`)
  else if (types.includes('string') && typeof value !== 'string') failures.push(`${location}: expected string`)
}
for (const { path, method, operation } of operations) {
  if (!path.startsWith('/api/v1/')) failures.push(`${path}: unversioned path`)
  if (!operation.summary || !operation.description) failures.push(`${method} ${path}: missing description`)
  const inspect = (content, kind) => {
    for (const [type, media] of Object.entries(content || {})) if (type.includes('json')) {
      if (kind === 'request') jsonRequests++; else jsonResponses++
      if (media.example === undefined) failures.push(`${method} ${path}: missing ${kind} example`)
      else checkExample(media.schema, media.example, `${method} ${path} ${kind}`)
    }
  }
  inspect(operation.requestBody?.content, 'request')
  Object.values(operation.responses).forEach(item => inspect(item.content, 'response'))
}
if (api.components.securitySchemes.bearerAuth?.bearerFormat !== 'JWT') failures.push('Missing Bearer JWT security scheme')
if (!api.info.description.includes('X-XSRF-TOKEN')) failures.push('Missing session/CSRF instructions')
const swagger = await fetch('http://localhost:8080/swagger-ui/index.html')
if (!swagger.ok) failures.push('Swagger UI is unavailable')
const report = { testedAt: new Date().toISOString(), url, paths: Object.keys(api.paths).length,
  operations: operations.length, jsonRequests, jsonResponses, swaggerStatus: swagger.status,
  exampleValidation: 'JSON shapes and scalar types checked against the served schemas; illustrative values are not live fixture identifiers',
  passed: failures.length === 0, failures }
mkdirSync('docs/evidence/system-tests', { recursive: true })
writeFileSync('docs/evidence/system-tests/openapi.json', JSON.stringify(report, null, 2) + '\n')
console.log(JSON.stringify(report))
if (!report.passed) process.exitCode = 1
