import { useEffect, useState } from 'react'
import { api } from '../api.js'
import { EmptyState } from '../components/EmptyState.jsx'
import { navigate } from '../lib/navigation.js'

const labels = {
  DONE: 'Pago aprobado y reserva confirmada',
  REJECTED: 'Pago rechazado o cancelado',
  REFUND_PENDING: 'Pago recibido después del cierre: reembolso en proceso',
  REFUNDED: 'Pago reembolsado',
  REVIEW: 'Pago pendiente de conciliación',
}
export function PaymentReturnPage({ session }) {
  const [state, setState] = useState(null)
  const [error, setError] = useState('')
  const id = new URLSearchParams(window.location.search).get('paymentId')
  useEffect(() => {
    if (!session || !id) return
    let active = true
    let timer
    let attempts = 0
    const load = async () => {
      try {
        const result = await api.checkout(id)
        if (!active) return
        setState(result)
        if (!['DONE', 'REJECTED', 'REFUNDED'].includes(result.paymentStatus) && ++attempts < 10) timer = setTimeout(load, 3000)
      } catch (reason) { if (active) setError(reason.message) }
    }
    load()
    return () => { active = false; clearTimeout(timer) }
  }, [session, id])
  if (!session) return <EmptyState title="Ingresa para consultar tu pago" text="El retorno del checkout no acredita por sí solo una aprobación." action={() => navigate('/login')} actionText="Ingresar" />
  return <main className="page-shell page-top"><p className="kicker dark">Mercado Pago · ambiente de prueba</p><h1>Estado de tu pago</h1>
    {error ? <p className="form-error" role="alert">{error}</p> : <p role="status">{state ? labels[state.paymentStatus] || 'Esperando verificación del proveedor' : 'Consultando estado seguro…'}</p>}
    <p>El servidor verifica el importe y el estado informado por Mercado Pago. Usa únicamente compradores y medios de pago de prueba; el comprobante no tiene validez fiscal.</p>
    <button className="button button-dark" onClick={() => navigate('/cuenta')}>Consultar mis reservas</button></main>
}
