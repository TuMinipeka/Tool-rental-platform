export const catalogChangeKey = 'tool-rental-catalog-changed'

export function notifyCatalogChange() {
  window.dispatchEvent(new Event(catalogChangeKey))
  try {
    // Only an invalidation marker is shared between tabs; never account data.
    window.localStorage.setItem(catalogChangeKey, crypto.randomUUID())
  } catch {
    // Storage can be disabled. Focus and periodic revalidation still work.
  }
}
