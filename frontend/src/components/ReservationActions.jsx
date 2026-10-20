import { useFeatures } from '../lib/features.js'
import { useRef, useState } from 'react'
import { api } from '../api.js'
import { entityId } from '../lib/records.js'
import { navigate } from '../lib/navigation.js'

export function ReservationActions({ reservation, supplier = false, onChanged }) {
  const { hostedCheckout, paymentProvider } = useFeatures()
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [checkout, setCheckout] = useState(null)
  const checkoutKey = useRef(null)
  const cancellationKey = useRef(null)
  const id = entityId(reservation.id ?? reservation.reservationId)
  const pending = reservation.status === 'PENDING'
  const cancellable = !supplier && ['PENDING', 'PENDING_PAYMENT', 'CONFIRMED'].includes(reservation.status)
  const payable = !supplier && reservation.status === 'PENDING_PAYMENT'
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
  const requestCheckout = async () => {
    setBusy(true); setError(''); checkoutKey.current ??= crypto.randomUUID()
    try {
      const result = await api.createCheckout(id, checkoutKey.current)
      setCheckout(result)
      if (result.checkoutUrl && result.paymentStatus === 'READY') {
        const target = new URL(result.checkoutUrl, window.location.origin)
        if (result.processor === 'FICTIONAL_GATEWAY' && paymentProvider === 'FICTIONAL_GATEWAY') {
          if (target.origin !== window.location.origin || target.pathname !== '/checkout/' + result.paymentId) throw new Error('La dirección de la pasarela ficticia es inválida.')
          navigate(target.pathname)
          return
        }
        if (target.protocol !== 'https:' || !['www.mercadopago.com.co', 'www.mercadopago.com', 'mercadopago.com.co'].includes(target.hostname)) throw new Error('El proveedor devolvió una dirección de pago inválida.')
        window.location.assign(target.href)
      }
    }
    catch (reason) { setError(reason.message) }
    finally { setBusy(false) }
  }
  return <div className="reservation-actions" aria-busy={busy}>
    {supplier && pending && <><button className="button button-dark" disabled={busy} onClick={() => perform('confirmation')}>Confirmar solicitud</button><button className="button button-ghost" disabled={busy} onClick={() => perform('rejection')}>Rechazar solicitud</button></>}
    {payable && <button className="button button-dark" disabled={busy || !hostedCheckout} onClick={requestCheckout}>{paymentProvider === 'FICTIONAL_GATEWAY' ? 'Pagar en pasarela ficticia' : 'Continuar en Mercado Pago'}</button>}
    {payable && !hostedCheckout && <p role="status">La pasarela no está disponible en este entorno.</p>}
    {checkout && <p role="status">Estado del checkout: {checkout.paymentStatus}. La confirmación depende del estado persistido en el servidor. Si requiere revisión, no crees otra reserva para repetir el pago.</p>}
    {cancellable && <button className="button button-ghost" disabled={busy} onClick={() => perform('cancellation')}>Solicitar cancelación</button>}
    {error && <p className="form-error" role="alert">{error}</p>}
  </div>
}
