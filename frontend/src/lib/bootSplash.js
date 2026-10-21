// Matches --ts-appear in index.html: before this point the splash content is still invisible.
const APPEAR_MS = 300
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

function releaseBootSplash() {
  if (released) return
  released = true
  const splash = document.getElementById('boot-splash')
  if (!splash) return

  const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  const tape = splash.querySelector('.ts-tape')
  const visible = performance.now() >= APPEAR_MS

  if (!visible || reducedMotion || !tape) {
    leave(splash)
    return
  }

  // Freeze the tape where it is, then snap it back into the case before fading out.
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
