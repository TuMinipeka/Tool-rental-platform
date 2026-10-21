import { useEffect, useRef } from 'react'

const reducedMotion = typeof window === 'undefined' ? null : window.matchMedia('(prefers-reduced-motion: reduce)')

// Coalesce pointer bursts and perform reads before transform writes, once per frame.
export function usePointerFrame(update, reset) {
  const pending = useRef(0)
  const sample = useRef(null)
  const callbacks = useRef({ update, reset })
  useEffect(() => { callbacks.current = { update, reset } })
  useEffect(() => () => cancelAnimationFrame(pending.current), [])
  const clear = () => {
    cancelAnimationFrame(pending.current)
    pending.current = 0
    sample.current = null
  }
  return {
    onPointerMove(event) {
      if (event.pointerType === 'touch' || reducedMotion?.matches) return
      sample.current = { node: event.currentTarget, clientX: event.clientX, clientY: event.clientY }
      if (pending.current) return
      pending.current = requestAnimationFrame(() => {
        pending.current = 0
        const point = sample.current
        if (!point?.node.isConnected) return
        const bounds = point.node.getBoundingClientRect()
        const x = (point.clientX - bounds.left) / bounds.width - .5
        const y = (point.clientY - bounds.top) / bounds.height - .5
        callbacks.current.update(point.node, x, y)
      })
    },
    onPointerLeave(event) {
      clear()
      callbacks.current.reset(event.currentTarget)
    },
    onPointerCancel(event) {
      clear()
      callbacks.current.reset(event.currentTarget)
    },
  }
}
