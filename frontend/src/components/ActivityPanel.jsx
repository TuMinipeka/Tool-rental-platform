import { useEffect, useState } from 'react'
import { api } from '../api.js'
import { money } from '../lib/format.js'
import { entityId } from '../lib/records.js'
import { ReservationActions } from './ReservationActions.jsx'

const contracts = {
  rentals: { title: 'Alquileres', load: () => api.listRentals(), id: 'reservationId', status: 'rentalStatus' },
  payments: { title: 'Pagos registrados', load: () => api.listPayments(), id: 'paymentId', status: 'paymentStatus' },
  invoices: { title: 'Comprobantes académicos', load: () => api.listInvoices(), id: 'invoiceId', status: 'invoiceStatus' },
  reservations: { title: 'Gestión de reservas', load: () => api.listReservations(), id: 'id', status: 'status' },
}
export function ActivityPanel({ kind, supplier = false }) {
  const contract = contracts[kind]
  const [items, setItems] = useState([])
  const [state, setState] = useState('loading')
  const [error, setError] = useState('')
  const [revision, setRevision] = useState(0)
  useEffect(() => {
    let active = true
    contract.load().then(values => { if (active) { setItems(values); setState('ready') } })
      .catch(reason => { if (active) { setError(reason.message); setState('error') } })
    return () => { active = false }
  }, [contract, revision])
  return <section className="operations-table" aria-label={contract.title}>
    <div className="panel-heading"><div><h2>{contract.title}</h2><p>Registros devueltos por la API para tu usuario.</p></div><button className="button button-ghost" onClick={() => { setState('loading'); setRevision(value => value + 1) }}>Actualizar</button></div>
    {kind === 'payments' && <p className="panel-state">El registro de un pago no acredita una transacción sandbox. La pasarela verificable está pendiente de integración.</p>}
    {kind === 'invoices' && <p className="panel-state">Simulación académica: documento no válido como factura ante la DIAN. La descarga automática está pendiente de Conde.</p>}
    {state === 'loading' && <p role="status">Cargando registros…</p>}
    {state === 'error' && <p className="form-error" role="alert">{error}</p>}
    {state === 'ready' && !items.length && <p className="panel-state">No hay registros disponibles para tu usuario.</p>}
    {state === 'ready' && items.map(item => <article key={String(entityId(item[contract.id]))}>
      <div><span className="status-pill">{item[contract.status]}</span><h3>{item.number ?? String(entityId(item[contract.id])).slice(0, 8).toUpperCase()}</h3>
      {item.period && <p>{item.period.startDate} → {item.effectiveReturnDate ?? item.period.returnDate}</p>}
      {kind === 'reservations' && <ReservationActions reservation={item} supplier={supplier} onChanged={() => setRevision(value => value + 1)}/>}</div>
      {item.amount != null && <strong>{money.format(Number(item.amount))}</strong>}
    </article>)}
  </section>
}
