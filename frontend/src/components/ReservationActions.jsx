import { useRef, useState } from 'react'
import { api } from '../api.js'
import { entityId } from '../lib/records.js'

export function ReservationActions({ reservation, supplier = false, onChanged }) {
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const cancellationKey = useRef(null)
  const id = entityId(reservation.id ?? reservation.reservationId)
  const pending = reservation.status === 'PENDING'
  const cancellable = !supplier && ['PENDING', 'CONFIRMED'].includes(reservation.status)
  if (!(supplier && pending) && !cancellable) return null
  const perform = async action => {
    setBusy(true)
    setError('')
    if (action === 'cancellation') cancellationKey.current ??= crypto.randomUUID()
    try {
      await api.reservationAction(id, action, action === 'cancellation' ? cancellationKey.current : null)
      await onChanged()
    } catch (reason) {
      setError(reason.message + (reason.code ? ' (' + reason.code + ')' : ''))
    } finally { setBusy(false) }
  }
  return <div className="reservation-actions" aria-busy={busy}>
    {supplier && pending && <><button className="button button-dark" disabled={busy} onClick={() => perform('confirmation')}>Confirmar solicitud</button><button className="button button-ghost" disabled={busy} onClick={() => perform('rejection')}>Rechazar solicitud</button></>}
    {cancellable && <button className="button button-ghost" disabled={busy} onClick={() => perform('cancellation')}>Solicitar cancelación</button>}
    {error && <p className="form-error" role="alert">{error}</p>}
  </div>
}
