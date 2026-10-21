import { useState } from 'react'
import { resolveApiImageUrl } from '../api.js'
import { resolveToolVisual, resolveToolVisualClass } from '../toolVisuals.js'
import { heroImageSizes, heroImageSource, imageAttributes } from '../lib/responsiveImages.js'

export function ToolVisual({ tool, compact = false, priority = false, reuseHero = false }) {
  const [failedUrls, setFailedUrls] = useState([])
  const imageUrl = [resolveApiImageUrl(tool.imageUrl), resolveToolVisual(tool)].find(url => url && !failedUrls.includes(url))
  const sizes = reuseHero && imageUrl === heroImageSource ? heroImageSizes : compact
    ? '(max-width: 600px) 90vw, (max-width: 1100px) 45vw, 400px'
    : '(max-width: 900px) 90vw, 50vw'
  return <div className={`tool-visual tool-${tool.color} ${resolveToolVisualClass(tool)} ${compact ? 'compact' : ''}`}>
    {imageUrl ? <img {...imageAttributes(imageUrl, sizes, () => setFailedUrls(current => [...current, imageUrl]))}
      alt={tool.name} loading={priority ? 'eager' : 'lazy'} decoding="async"/>
      : <><div className="visual-grid"/><div className="tool-silhouette" aria-hidden="true"><span/><span/><span/></div></>}
    <b>{tool.category}</b><small>TR—{tool.id.slice(0, 3).toUpperCase()}</small>
  </div>
}
