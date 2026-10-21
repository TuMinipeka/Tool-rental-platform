// The app loads and renders behind the splash during this minimum time on every full page load.
const MIN_DISPLAY_MS = 4000
const RETRACT_MS = 260
const LEAVE_MS = 420
const APP_ENTER_MS = 520
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

function releaseBootSplash() {
  if (released) return
  released = true
  const splash = document.getElementById('boot-splash')
  if (!splash) return

  const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  const wait = Math.max(0, MIN_DISPLAY_MS - performance.now())
  window.setTimeout(() => (reducedMotion ? leave(splash) : retract(splash)), wait)
}

// Freeze the tape where it is, then snap it back into the case before revealing the app.
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

// Dissolve the splash while the already-rendered app rises into place.
function leave(splash) {
  const root = document.documentElement
  const remove = () => splash.remove()
  splash.setAttribute('aria-hidden', 'true')
  root.classList.add('app-entering')
  splash.classList.add('is-leaving')
  splash.addEventListener('transitionend', event => { if (event.target === splash) remove() })
  window.setTimeout(remove, LEAVE_MS + 120)
  window.setTimeout(() => root.classList.remove('app-entering'), APP_ENTER_MS + 80)
}
