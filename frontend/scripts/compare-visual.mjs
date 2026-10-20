import { readFile, readdir, writeFile } from 'node:fs/promises'
import { resolve } from 'node:path'
import sharp from 'sharp'

const before = process.argv[2] || 'baseline'
const after = process.argv[3] || 'optimized'
for (const label of [before, after]) if (!/^[a-z0-9-]+$/.test(label)) throw new Error('Use lowercase audit labels')
const beforeRoot = resolve('test-results', before)
const afterRoot = resolve('test-results', after)
const baseline = JSON.parse(await readFile(resolve(beforeRoot, 'browser.json')))
const current = JSON.parse(await readFile(resolve(afterRoot, 'browser.json')))
const geometry = baseline.routes.filter(route => route.geometry).map(route => {
  const match = current.routes.find(next => next.path === route.path && next.profile === route.profile)
  const deltas = route.geometry.map((rect, index) => {
    const next = match?.geometry[index]
    if (!next || next.selector !== rect.selector) return Infinity
    return Math.max(...['x', 'y', 'width', 'height'].map(key => Math.abs(rect[key] - next[key])))
  })
  return { path: route.path, profile: route.profile, maxGeometryDelta: Math.max(0, ...deltas) }
})
const screenshots = []
for (const file of (await readdir(beforeRoot)).filter(file => file.endsWith('.png'))) {
  const first = await sharp(resolve(beforeRoot, file)).ensureAlpha().raw().toBuffer({ resolveWithObject: true })
  const second = await sharp(resolve(afterRoot, file)).ensureAlpha().raw().toBuffer({ resolveWithObject: true })
  if (first.info.width !== second.info.width || first.info.height !== second.info.height) {
    screenshots.push({ file, dimensionsMatch: false, before: first.info, after: second.info })
    continue
  }
  let totalDifference = 0
  let changedPixels = 0
  for (let index = 0; index < first.data.length; index += 4) {
    const differences = [0, 1, 2].map(channel => Math.abs(first.data[index + channel] - second.data[index + channel]))
    totalDifference += differences.reduce((sum, value) => sum + value, 0)
    if (Math.max(...differences) > 16) changedPixels++
  }
  screenshots.push({ file, dimensionsMatch: true,
    normalizedMeanAbsoluteError: totalDifference / (first.info.width * first.info.height * 3 * 255),
    changedPixelFraction: changedPixels / (first.info.width * first.info.height) })
}
const report = { before, after, geometry, screenshots,
  note: 'Color contrast corrections and responsive-image resampling can change pixels. Inspect images; metrics do not replace visual review.' }
await writeFile(resolve(afterRoot, 'visual-comparison.json'), JSON.stringify(report, null, 2))
console.log(JSON.stringify({ geometryDrift: geometry.filter(row => row.maxGeometryDelta > .5),
  changedDimensions: screenshots.filter(row => !row.dimensionsMatch),
  screenshots: screenshots.filter(row => row.dimensionsMatch).map(row => ({ file: row.file, meanError: row.normalizedMeanAbsoluteError })) }))
if (geometry.some(row => row.maxGeometryDelta > .5) || screenshots.some(row => !row.dimensionsMatch)) process.exitCode = 1
