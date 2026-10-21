import { useCallback, useEffect, useRef, useState } from 'react'
import { api, normalizeCatalogTool } from '../api.js'
import { demoCatalog } from '../demoCatalog.js'
import { catalogChangeKey } from '../lib/catalogChanges.js'

const demoMode = import.meta.env.VITE_DEMO_MODE === 'true'

export function usePublicCatalog() {
  const [catalogTools, setCatalogTools] = useState(demoMode ? demoCatalog : [])
  const [dataMode, setDataMode] = useState(demoMode ? 'demo' : 'loading')
  const [catalogError, setCatalogError] = useState('')
  const refreshRef = useRef(() => {})
  const refreshCatalog = useCallback(() => refreshRef.current(), [])

  useEffect(() => {
    let active = true
    let inFlight = null
    let queued = false
    const refresh = () => {
      if (inFlight) { queued = true; return inFlight }
      inFlight = api.listCatalog().then(items => {
        if (!active) return
        if (items.length || !demoMode) setCatalogTools(items.map(normalizeCatalogTool))
        setDataMode(demoMode ? 'demo' : 'api')
        setCatalogError('')
      }).catch(() => {
        if (!active) return
        setCatalogError('No pudimos actualizar el catálogo. Los datos visibles pueden estar desactualizados.')
        setDataMode(demoMode ? 'demo' : 'error')
      }).finally(() => {
        inFlight = null
        if (active && queued) { queued = false; refresh() }
      })
      return inFlight
    }
    const isCatalogRoute = () => /^(\/|\/catalogo|\/herramienta\/[^/]+)$/.test(window.location.pathname)
    const refreshVisible = () => {
      if (document.visibilityState === 'visible' && isCatalogRoute()) refresh()
    }
    const storageChanged = event => { if (event.key === catalogChangeKey) refreshVisible() }
    refreshRef.current = refresh
    window.addEventListener('popstate', refreshVisible)
    window.addEventListener('focus', refreshVisible)
    window.addEventListener(catalogChangeKey, refresh)
    window.addEventListener('storage', storageChanged)
    document.addEventListener('visibilitychange', refreshVisible)
    const interval = window.setInterval(refreshVisible, 30000)
    refresh()
    return () => {
      active = false
      refreshRef.current = () => {}
      window.clearInterval(interval)
      window.removeEventListener('popstate', refreshVisible)
      window.removeEventListener('focus', refreshVisible)
      window.removeEventListener(catalogChangeKey, refresh)
      window.removeEventListener('storage', storageChanged)
      document.removeEventListener('visibilitychange', refreshVisible)
    }
  }, [])

  return { catalogTools, dataMode, catalogError, refreshCatalog }
}
