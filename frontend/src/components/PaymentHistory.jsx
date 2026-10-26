import { useEffect, useState } from 'react'
import { api } from '../api.js'
import { money } from '../lib/format.js'

const statusLabels = {
  CONFIRMED: 'Pagado',
  PENDING: 'Pendiente',
  FAILED: 'Fallido',
  REFUNDED: 'Reembolsado',
}

export function PaymentHistory({ userId }) {
  const [payments, setPayments] = useState([])
  const [state, setState] = useState('loading')
  const [error, setError] = useState('')
  const [revision, setRevision] = useState(0)

  useEffect(() => {
    let active = true
    api.customerPaymentHistory(userId).then(items => {
      if (active) { setPayments(items); setState('ready') }
    }).catch(reason => {
      if (active) { setError(reason.message); setState('error') }
    })
    return () => { active = false }
  }, [userId, revision])

  return <section className="operations-table" aria-labelledby="payment-history-title">
    <div className="panel-heading">
      <h2 id="payment-history-title">Historial de Pagos</h2>
      <button className="button button-ghost" disabled={state === 'loading'} onClick={() => {
        setState('loading'); setError(''); setRevision(value => value + 1)
      }}>Actualizar</button>
    </div>
    {state === 'loading' && <p role="status">Cargando pagos…</p>}
    {state === 'error' && <p className="form-error" role="alert">{error}</p>}
    {state === 'ready' && !payments.length && <p>Aún no tienes pagos registrados.</p>}
    {state === 'ready' && payments.length > 0 && <div className="report-table-wrap">
      <table className="report-table">
        <caption>Pagos de tu cuenta · Valores en COP</caption>
        <thead><tr><th scope="col">Fecha</th><th scope="col">Monto</th><th scope="col">Estado</th></tr></thead>
        <tbody>{payments.map(payment => <tr key={payment.paymentId}>
          <td>{new Date(payment.paymentDate).toLocaleDateString('es-CO', { timeZone: 'America/Bogota' })}</td>
          <td>{money.format(Number(payment.amount))}</td>
          <td>{statusLabels[payment.status] || payment.status}</td>
        </tr>)}</tbody>
      </table>
    </div>}
  </section>
}
