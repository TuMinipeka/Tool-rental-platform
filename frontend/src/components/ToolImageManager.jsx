import { useEffect, useState } from 'react'
import { api, resolveApiImageUrl } from '../api.js'
import { entityId } from '../lib/records.js'

export function ToolImageManager({ tool, onChanged, onClose }) {
  const id = entityId(tool.id)
  const [images, setImages] = useState(tool.imageUrls || [])
  const [url, setUrl] = useState('')
  const [busy, setBusy] = useState(false)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  useEffect(() => {
    let active = true
    api.listToolImages(id).then(rows => { if (active) setImages(rows.map(row => row.imageUrl)) })
      .catch(reason => { if (active) setError(reason.message) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [id])
  const mutate = async operation => {
    setBusy(true); setError(''); setMessage('')
    try {
      await operation()
      const rows = await api.listToolImages(id)
      setImages(rows.map(row => row.imageUrl))
      await onChanged(await api.getTool(id))
      setMessage('Imágenes guardadas en el servidor.')
      return true
    } catch (reason) { setError(reason.message); return false }
    finally { setBusy(false) }
  }
  const upload = async event => {
    const input = event.currentTarget, file = input.files?.[0]
    if (!file) return
    if (!['image/jpeg', 'image/png'].includes(file.type) || file.size > 5 * 1024 * 1024) { setError('Selecciona una imagen JPEG o PNG de hasta 5 MiB.'); return }
    if (await mutate(() => api.uploadToolImage(id, file))) input.value = ''
  }
  const addUrl = async () => {
    if (await mutate(() => api.addToolImage(id, url.trim()))) setUrl('')
  }
  return <section className="tool-image-manager" aria-label={`Imágenes de ${tool.name}`} aria-busy={busy}>
    <div className="panel-heading"><div><h2>Imágenes de {tool.name}</h2><p>Hasta 8 fotos públicas. JPEG o PNG, máximo 5 MiB y 4096 × 4096 píxeles por archivo.</p></div><button type="button" className="text-action" onClick={onClose} disabled={busy}>Cerrar imágenes</button></div>
    {error && <p role="alert" className="form-error">{error}</p>}{message && <p role="status">{message}</p>}
    {loading ? <p role="status">Cargando imágenes…</p> : <>
      <div className="tool-image-grid">{images.map(image => <figure key={image}><img src={resolveApiImageUrl(image)} alt={`Foto de ${tool.name}`} loading="lazy"/><button type="button" className="danger-action" disabled={busy} onClick={() => mutate(() => api.removeToolImage(id, image))}>Retirar imagen</button></figure>)}</div>
      {!images.length && <p>No hay imágenes asociadas. Guarda primero la herramienta y añade sus fotos aquí.</p>}
      <label className="form-field"><span>Subir fotografía</span><input type="file" accept="image/jpeg,image/png" onChange={upload} disabled={busy || images.length >= 8}/></label>
      <div className="image-url-row"><label className="form-field"><span>URL de imagen</span><input type="url" value={url} onChange={event => setUrl(event.target.value)} placeholder="https://…" disabled={busy || images.length >= 8}/></label><button type="button" className="button button-dark" disabled={busy || !url.trim() || images.length >= 8} onClick={addUrl}>Añadir URL</button></div>
    </>}
  </section>
}
