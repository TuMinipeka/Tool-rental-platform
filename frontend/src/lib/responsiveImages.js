import { responsiveImages } from '../generated/responsiveImages.js'

export const heroImageSource = '/assets/hero-rotary-hammer-1200.png'
export const heroImageSizes = '(max-width: 700px) 120vw, 55vw'

// Cache only the finite, build-generated assets; never retain arbitrary API URLs.
const attributes = new Map()

export function imageAttributes(src, sizes = '100vw', onError) {
  const asset = responsiveImages[src]
  if (!asset) return { src, onError }
  let image = attributes.get(src)
  if (!image) {
    const stem = src.slice(src.lastIndexOf('/') + 1, -4)
    const url = (width, hash) => `/assets/optimized/${stem}-${width}-${hash}.webp`
    image = { src: url(...asset.variants.at(-1)),
      srcSet: asset.variants.map(([width, hash]) => `${url(width, hash)} ${width}w`).join(', '),
      width: asset.width, height: asset.height }
    attributes.set(src, image)
  }
  return { ...image, sizes, onError(event) {
    const image = event.currentTarget
    if (image.hasAttribute('srcset')) {
      // Keep the original PNG fallback without adding React state/fibers per image.
      image.removeAttribute('srcset')
      image.src = src
    } else onError?.(event)
  } }
}
