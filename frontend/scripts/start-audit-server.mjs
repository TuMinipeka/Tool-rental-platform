import { execFileSync } from 'node:child_process'
import { mkdir, readFile, writeFile } from 'node:fs/promises'
import { resolve } from 'node:path'

const root = resolve(import.meta.dirname, '..')
const results = resolve(root, 'test-results')
await mkdir(results, { recursive: true })
const source = await readFile(process.env.AUDIT_CONFIG_SOURCE || resolve(root, 'nginx.conf'), 'utf8')
// Keep production resource delivery; route the API to the existing local backend.
const config = source.replace(/}\s*$/, `
    location /api/ {
        proxy_pass http://host.docker.internal:8080;
        proxy_set_header Host $http_host;
    }
}
`)
const configPath = resolve(results, 'audit-nginx.conf')
await writeFile(configPath, config)
const name = 'tool-rental-frontend-audit'
try { execFileSync('docker', ['rm', '-f', name], { stdio: 'ignore' }) } catch { /* First run. */ }
execFileSync('docker', ['run', '-d', '--name', name,
  '-p', '127.0.0.1:4173:80',
  '--mount', `type=bind,source=${process.env.AUDIT_DIST || resolve(root, 'dist')},target=/usr/share/nginx/html,readonly`,
  '--mount', `type=bind,source=${configPath},target=/etc/nginx/conf.d/default.conf,readonly`,
  process.env.AUDIT_NGINX_IMAGE || 'tool-rental-platform-frontend:latest'], { stdio: 'inherit' })
execFileSync('docker', ['exec', name, 'nginx', '-t'], { stdio: 'inherit' })
console.log('Production audit server: http://localhost:4173 (local backend on 8080)')
