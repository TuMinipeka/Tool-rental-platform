const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)')

export function createPointerMotion(update, reset) {
  const active = new WeakMap()
  const leave = event => {
    const node = event.currentTarget
    const state = active.get(node)
    if (state) cancelAnimationFrame(state.frame)
    active.delete(node)
    reset(node)
  }
  return {
    onPointerMove(event) {
      if (event.pointerType === 'touch' || reducedMotion.matches) return
      const node = event.currentTarget
      let state = active.get(node)
      if (!state) { state = { frame: 0 }; active.set(node, state) }
      state.x = event.clientX
      state.y = event.clientY
      if (state.frame) return
      state.frame = requestAnimationFrame(() => {
        state.frame = 0
        // Discard a queued update if navigation has already unmounted this element.
        if (!node.isConnected) { active.delete(node); return }
        const bounds = node.getBoundingClientRect()
        update(node, (state.x - bounds.left) / bounds.width - .5, (state.y - bounds.top) / bounds.height - .5)
      })
    },
    onPointerLeave: leave,
    onPointerCancel: leave,
  }
}
