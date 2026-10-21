import { useFeatures } from '../lib/features.js'
import { useRef, useState } from 'react'
import { api } from '../api.js'
import { navigate } from '../lib/navigation.js'
import { entityId } from '../lib/records.js'

const nextIsoDate=value=>{const date=new Date(`${value}T00:00:00Z`);date.setUTCDate(date.getUTCDate()+1);return date.toISOString().slice(0,10)}

export function ReservationActions({ reservation, supplier = false, onChanged }) {
  const { hostedCheckout } = useFeatures()
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const cancellationKey = useRef(null)
  const extensionKey = useRef(null)
  const [extensionOpen,setExtensionOpen] = useState(false)
  const id = entityId(reservation.id ?? reservation.reservationId)
  const pending = reservation.status === 'PENDING'
  const cancellable = !supplier && ['PENDING', 'PENDING_PAYMENT', 'CONFIRMED'].includes(reservation.status)
  const payable = !supplier && reservation.status === 'PENDING_PAYMENT'
  const extendable = !supplier && reservation.status === 'CONFIRMED'
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
  const requestExtension=async event=>{
    event.preventDefault()
    const formElement=event.currentTarget
    const requestedReturnDate=new FormData(formElement).get('requestedReturnDate')
    setBusy(true);setError('');extensionKey.current??=crypto.randomUUID()
    try{await api.requestReservationExtension(id,requestedReturnDate,extensionKey.current);setExtensionOpen(false);await onChanged()}
    catch(reason){setError(reason.message+(reason.code?' ('+reason.code+')':''))}
    finally{setBusy(false)}
  }
  return <div className="reservation-actions" aria-busy={busy}>
    {supplier && pending && <><button className="button button-dark" disabled={busy} onClick={() => perform('confirmation')}>Confirmar solicitud</button><button className="button button-ghost" disabled={busy} onClick={() => perform('rejection')}>Rechazar solicitud</button></>}
    {payable && <button className="button button-dark" disabled={busy || !hostedCheckout} onClick={() => navigate(`/pago?reservationId=${encodeURIComponent(id)}`)}>Elegir medio de pago</button>}
    {payable && !hostedCheckout && <p role="status">La pasarela no está disponible en este entorno.</p>}
    {cancellable && <button className="button button-ghost" disabled={busy} onClick={() => perform('cancellation')}>Solicitar cancelación</button>}
    {extendable&&<button className="button button-ghost" disabled={busy} onClick={()=>setExtensionOpen(value=>!value)}>{extensionOpen?'Cerrar extensión':'Solicitar extensión'}</button>}
    {extensionOpen&&<form className="reservation-extension-form" onSubmit={requestExtension}><label><span>Nueva fecha de devolución</span><input name="requestedReturnDate" type="date" min={nextIsoDate(reservation.effectiveReturnDate||reservation.period?.returnDate)} required/></label><button className="button button-dark" disabled={busy}>Enviar solicitud</button></form>}
    {error && <p className="form-error" role="alert">{error}</p>}
  </div>
}
