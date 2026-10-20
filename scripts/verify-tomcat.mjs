import { execFileSync } from 'node:child_process'
import { writeFileSync } from 'node:fs'
import { sourceFingerprint } from './source-fingerprint.mjs'
const compose = ['compose', '--env-file', '.env.validation', '-f', 'compose.validation.yml']
const run = args => execFileSync('docker', args, { encoding: 'utf8' }).trim()
const container = run([...compose, 'ps', '-q', 'backend'])
if (!container) throw new Error('The isolated backend is not running')
const entrypoint = JSON.parse(run(['inspect', '--format', '{{json .Config.Entrypoint}}', container]))
if (entrypoint[0] !== 'java' || entrypoint[1] !== '-jar') throw new Error('Expected executable Spring Boot JAR')
const server = run([...compose, 'exec', '-T', 'backend', 'java', '-cp', 'lib/*', 'org.apache.catalina.util.ServerInfo'])
const version = server.split(/\r?\n/).find(line => /Server version:/.test(line))?.trim()
if (!version?.includes('Apache Tomcat/')) throw new Error('Apache Tomcat runtime was not found')
const response = await fetch('http://localhost:18080/status/database')
if (!response.ok || !(await response.json()).connected) throw new Error('Embedded server is not serving the real JDBC check')
writeFileSync('docs/evidence/mvp/tomcat-runtime.json', JSON.stringify({ testedAt: new Date().toISOString(), sourceFingerprint: sourceFingerprint(), entrypoint, serverVersion: version, internalPort: 8080, databaseConnected: true, standaloneWarRequired: false, publicDeploymentPerformed: false }, null, 2) + '\n')
console.log('Verified embedded runtime: ' + version)
