import { useEffect, useState } from 'react'

// Dashboard sections shown in the header once a provider or administrator is signed in.
// Permissions are still enforced by the backend; these entries only decide navigation.
export const dashboardSections = {
  provider: {
    path: '/panel/proveedor',
    items: [['summary', 'Resumen', 'grid', 'resumen'], ['tools', 'Herramientas', 'box', 'herramientas']],
  },
  admin: {
    path: '/panel/admin',
    items: [['summary', 'Resumen', 'grid', 'resumen'], ['providers', 'Proveedores', 'box', 'proveedores'], ['administrators', 'Administradores', 'shield', 'administradores']],
  },
}

export function staffDashboard(session) {
  if (session?.roles?.includes('ADMIN')) return dashboardSections.admin
  if (session?.roles?.includes('SUPPLIER')) return dashboardSections.provider
  return null
}

export function dashboardSectionHref(dashboard, id) {
  const slug = dashboard.items.find(([itemId]) => itemId === id)?.[3]
  return id === 'summary' || !slug ? dashboard.path : `${dashboard.path}?seccion=${slug}`
}

function readSection(dashboard) {
  if (!dashboard || window.location.pathname !== dashboard.path) return null
  const slug = new URLSearchParams(window.location.search).get('seccion')
  return dashboard.items.find(item => item[3] === slug)?.[0] || 'summary'
}

// Active section derived from the URL, so the header and the dashboard stay in sync.
export function useDashboardSection(dashboard) {
  const [section, setSection] = useState(() => readSection(dashboard))
  useEffect(() => {
    const update = () => setSection(readSection(dashboard))
    window.addEventListener('popstate', update)
    return () => window.removeEventListener('popstate', update)
  }, [dashboard])
  return section
}
