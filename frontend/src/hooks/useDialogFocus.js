import { useEffect, useRef } from 'react'

export function useDialogFocus(onClose) {
  const ref = useRef(null)
  const close = useRef(onClose)
  useEffect(() => { close.current = onClose }, [onClose])
  useEffect(() => {
    const dialog = ref.current
    const trigger = document.activeElement
    const overflow = document.body.style.overflow
    const backgrounds = [...document.querySelectorAll('.site-header, .page-content, .site-footer')]
      .map(node => ({ node, inert: node.inert }))
    backgrounds.forEach(({ node }) => { node.inert = true })
    document.body.style.overflow = 'hidden'
    dialog.querySelector('input, button')?.focus()
    const keydown = event => {
      if (event.key === 'Escape') { event.preventDefault(); close.current(); return }
      if (event.key !== 'Tab') return
      const focusable = [...dialog.querySelectorAll('button:not([disabled]), a[href], input:not([disabled]), select:not([disabled]), [tabindex="0"]')]
        .filter(node => node.getClientRects().length)
      const first = focusable[0]
      const last = focusable.at(-1)
      if (event.shiftKey && (document.activeElement === first || !dialog.contains(document.activeElement))) {
        event.preventDefault(); last?.focus()
      } else if (!event.shiftKey && (document.activeElement === last || !dialog.contains(document.activeElement))) {
        event.preventDefault(); first?.focus()
      }
    }
    document.addEventListener('keydown', keydown)
    return () => {
      document.removeEventListener('keydown', keydown)
      backgrounds.forEach(({ node, inert }) => { node.inert = inert })
      document.body.style.overflow = overflow
      if (trigger?.isConnected) trigger.focus({ preventScroll: true })
    }
  }, [])
  return ref
}
