const SEEN_KEY = 'toolrental-splash-seen'
// First visit of the browser session: keep the tape on screen long enough to read it (about 60 cm).
const FIRST_VISIT_MIN_MS = 1800
// Matches .boot-splash--returning in index.html: before this point the splash content is still invisible.
const RETURNING_APPEAR_MS = 300
const RETRACT_MS = 260
const LEAVE_MS = 220
const SNAP_EASING = 'cubic-bezier(0.77, 0, 0.175, 1)'

let released = false

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

function markSeen() {
  try {
    window.sessionStorage.setItem(SEEN_KEY, '1')
  } catch {
    // Storage can be unavailable (private mode); the splash still works without it.
  }
}

function releaseBootSplash() {
  if (released) return
  released = true
  const splash = document.getElementById('boot-splash')
  if (!splash) return

  const returning = splash.classList.contains('boot-splash--returning')
  const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  markSeen()

  if (returning) {
    if (performance.now() < RETURNING_APPEAR_MS || reducedMotion) leave(splash)
    else retract(splash)
    return
  }
  const wait = reducedMotion ? 0 : Math.max(0, FIRST_VISIT_MIN_MS - performance.now())
  window.setTimeout(() => (reducedMotion ? leave(splash) : retract(splash)), wait)
}

// Freeze the tape where it is, then snap it back into the case before fading out.
function retract(splash) {
  const tape = splash.querySelector('.ts-tape')
  if (!tape) {
    leave(splash)
    return
  }
  const width = tape.getBoundingClientRect().width
  tape.getAnimations().forEach(animation => animation.cancel())
  tape.style.width = `${width}px`
  splash.classList.add('is-retracting')
  tape.animate([{ width: `${width}px` }, { width: '0px' }], { duration: RETRACT_MS, easing: SNAP_EASING, fill: 'forwards' })
  splash.querySelector('.ts-case')?.animate(
    [{ transform: 'rotate(0deg)' }, { transform: 'rotate(9deg)', offset: 0.75 }, { transform: 'rotate(0deg)' }],
    { duration: RETRACT_MS + 120, easing: 'ease-out' },
  )
  window.setTimeout(() => leave(splash), RETRACT_MS)
}

function leave(splash) {
  const remove = () => splash.remove()
  splash.setAttribute('aria-hidden', 'true')
  splash.classList.add('is-leaving')
  splash.addEventListener('transitionend', remove, { once: true })
  window.setTimeout(remove, LEAVE_MS + 120)
}
