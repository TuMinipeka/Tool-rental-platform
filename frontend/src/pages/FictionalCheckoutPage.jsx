import { useEffect, useState } from 'react'
import { api } from '../api.js'
import { EmptyState } from '../components/EmptyState.jsx'
import { Icon } from '../components/Icon.jsx'
import { navigate } from '../lib/navigation.js'
import { money } from '../lib/format.js'

export function FictionalCheckoutPage({ session, paymentId }) {
  const [checkout, setCheckout] = useState(null)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  useEffect(() => {
    if (!session) return
    let active = true
    api.checkout(paymentId).then(value => {
      if (active) {
        if (value.processor !== 'FICTIONAL_GATEWAY') setError('Este pago no pertenece a la pasarela ficticia.')
        else setCheckout(value)
      }
    }).catch(reason => { if (active) setError(reason.message) })
    return () => { active = false }
  }, [session, paymentId])
  const decide = async outcome => {
    setBusy(true); setError('')
    try {
      const result = await api.decideFictionalPayment(paymentId, outcome)
      if (['DONE', 'REJECTED', 'REFUNDED'].includes(result.paymentStatus)) navigate('/payment-return?paymentId=' + encodeURIComponent(paymentId))
      else setCheckout(result)
    } catch (reason) { setError(reason.message) }
    finally { setBusy(false) }
  }
  if (!session) return <EmptyState title="Ingresa para continuar tu pago de prueba" text="La pasarela ficticia solo permite pagar las reservas de tu cuenta." action={() => navigate('/login?returnTo=' + encodeURIComponent('/checkout/' + paymentId))} actionText="Ingresar" />
  return <main className="payment-reference-page">
    <section className="payment-reference-hero"><div className="payment-reference-grid"/><div className="page-shell"><div><p>Demo académica · sin cobros reales</p><h1>Pasarela ficticia</h1><span>Prueba una aprobación o un rechazo. Nunca ingreses datos bancarios.</span></div><img src="/assets/hero-rotary-hammer-1200.png" alt=""/></div></section>
    <div className="payment-reference-layout page-shell"><section className="payment-reference-form" aria-busy={busy}>
      <div className="payment-demo-notice"><span>DEMO</span><p><strong>Esta operación es una simulación.</strong> No contacta bancos ni proveedores externos y no mueve dinero.</p></div>
      <h2>Elige un resultado de prueba</h2>
      {error && <p className="form-error" role="alert">{error}</p>}
      {!checkout && !error && <p role="status">Consultando el importe de la reserva…</p>}
      {checkout && <><p>El backend comprobará la reserva, su vencimiento y la disponibilidad antes de confirmar.</p>
        {checkout.paymentStatus === 'READY' ? <div className="success-actions"><button className="button button-dark" disabled={busy} onClick={() => decide('APPROVED')}>Simular aprobación <Icon name="check"/></button><button className="button button-ghost" disabled={busy} onClick={() => decide('DECLINED')}>Simular rechazo <Icon name="close"/></button></div>
          : <p role="status">Estado: {checkout.paymentStatus}. Consulta el resultado persistido en tu cuenta.</p>}
        <div className="payment-data-boundary"><Icon name="shield"/><div><strong>Sin números de tarjeta, CVV ni contraseñas bancarias</strong><p>El importe lo calcula el servidor; la decisión de prueba queda registrada.</p></div></div></>}
    </section><aside className="payment-reference-summary"><h2>Resumen del pago ficticio</h2>
      {checkout && <><p>Reserva {checkout.reservationId}</p><div className="payment-reference-total"><span>Total académico · {checkout.currency}</span><strong>{money.format(checkout.amount)}</strong></div><p>Válido hasta {new Date(checkout.expiresAt).toLocaleString('es-CO')}</p></>}
      <p>El comprobante es académico y no tiene validez fiscal.</p><button className="button button-ghost" onClick={() => navigate('/cuenta')}>Volver a mis reservas</button>
    </aside></div>
  </main>
}
