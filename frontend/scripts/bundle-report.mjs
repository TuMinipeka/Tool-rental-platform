import { mkdir, readFile, readdir, stat, writeFile } from 'node:fs/promises'
import { resolve } from 'node:path'
import { gzipSync } from 'node:zlib'

const label = process.argv[2] || 'current'
if (!/^[a-z0-9-]+$/.test(label)) throw new Error('Use a lowercase audit label')
const html = await readFile('dist/index.html', 'utf8')
const initial = new Set([...html.matchAll(/(?:src|href)="\/(assets\/[^"?]+\.(?:js|css))"/g)].map(match => match[1]))
const assets = []
for (const file of await readdir('dist/assets')) {
  if (!/\.(js|css)$/.test(file)) continue
  const content = await readFile(resolve('dist/assets', file))
  assets.push({ file, initial: initial.has(`assets/${file}`), bytes: content.length, gzipBytes: gzipSync(content).length })
}
const images = await readdir('public/assets', { recursive: true })
let imageBytes = 0
for (const path of images.filter(path => path.endsWith('.png'))) imageBytes += (await stat(resolve('public/assets', path))).size
const report = { assets, originalImageBytes: imageBytes,
  initialJavaScriptBytes: assets.filter(a => a.initial && a.file.endsWith('.js')).reduce((sum, a) => sum + a.bytes, 0),
  initialJavaScriptGzipBytes: assets.filter(a => a.initial && a.file.endsWith('.js')).reduce((sum, a) => sum + a.gzipBytes, 0),
  totalJavaScriptBytes: assets.filter(a => a.file.endsWith('.js')).reduce((sum, a) => sum + a.bytes, 0),
}
await mkdir(resolve('test-results', label), { recursive: true })
await writeFile(resolve('test-results', label, 'bundle.json'), JSON.stringify(report, null, 2))
console.log(JSON.stringify(report))
