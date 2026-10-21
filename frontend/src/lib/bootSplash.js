const SEEN_KEY = 'toolrental-splash-seen'
// One full gag (taps, lightning and reassembly) on the first visit of the browser session.
const FIRST_VISIT_MIN_MS = 2600
const LEAVE_FALLBACK_MS = 600

let released = false

function readSeen() {
  try {
    const seen = window.sessionStorage.getItem(SEEN_KEY) === '1'
    window.sessionStorage.setItem(SEEN_KEY, '1')
    return seen
  } catch {
    return false
  }
}

export function releaseBootSplashWhenRendered(rootElement) {
  if (rootElement.childElementCount > 0) {
    releaseBootSplash()
    return
  }
  const observer = new MutationObserver(() => {
    if (rootElement.childElementCount === 0) return
    observer.disconnect()
    releaseBootSplash()
  })
  observer.observe(rootElement, { childList: true })
}

function releaseBootSplash() {
  if (released) return
  released = true
  const splash = document.getElementById('boot-splash')
  if (!splash) return

  const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  const minimum = readSeen() || reducedMotion ? 0 : FIRST_VISIT_MIN_MS
  const wait = Math.max(0, minimum - performance.now())

  window.setTimeout(() => {
    const remove = () => splash.remove()
    splash.setAttribute('aria-hidden', 'true')
    splash.classList.add('is-leaving')
    splash.addEventListener('transitionend', remove, { once: true })
    window.setTimeout(remove, LEAVE_FALLBACK_MS)
  }, wait)
}
