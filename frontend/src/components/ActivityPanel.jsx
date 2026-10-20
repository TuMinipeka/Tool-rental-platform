import { ReservationWorkflow } from './ReservationWorkflow.jsx'
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
  const download = async id => {
    try { const html=await api.invoiceDocument(id); const url=URL.createObjectURL(new Blob([html],{type:'text/html'})); const link=document.createElement('a'); link.href=url; link.download='academic-invoice-'+id+'.html'; link.click(); setTimeout(()=>URL.revokeObjectURL(url),1000) }
    catch(reason){setError(reason.message);setState('error')}
  }
  const [revision, setRevision] = useState(0)
  useEffect(() => {
    let active = true
    contract.load().then(values => { if (active) { setItems(values); setState('ready') } })
      .catch(reason => { if (active) { setError(reason.message); setState('error') } })
    return () => { active = false }
  }, [contract, revision])
  return <section className="operations-table" aria-label={contract.title}>
    <div className="panel-heading"><div><h2>{contract.title}</h2><p>Registros devueltos por la API para tu usuario.</p></div><button className="button button-ghost" onClick={() => { setState('loading'); setRevision(value => value + 1) }}>Actualizar</button></div>
    {kind === 'payments' && <p className="panel-state">Los pagos se procesan con Mercado Pago en ambiente de prueba. El servidor verifica su estado antes de confirmar la reserva.</p>}
    {kind === 'invoices' && <p className="panel-state">Documento no válido como factura ante la DIAN. Descarga el comprobante emitido por el backend.</p>}
    {state === 'loading' && <p role="status">Cargando registros…</p>}
    {state === 'error' && <p className="form-error" role="alert">{error}</p>}
    {state === 'ready' && !items.length && <p className="panel-state">No hay registros disponibles para tu usuario.</p>}
    {state === 'ready' && items.map(item => <article key={String(entityId(item[contract.id]))}>
      <div><span className="status-pill">{item[contract.status]}</span><h3>{item.number ?? String(entityId(item[contract.id])).slice(0, 8).toUpperCase()}</h3>
      {item.period && <p>{item.period.startDate} → {item.effectiveReturnDate ?? item.period.returnDate}</p>}
      {kind === 'reservations' && <><ReservationActions reservation={item} supplier={supplier} onChanged={() => setRevision(value => value + 1)}/>{supplier&&item.status==='CONFIRMED'&&<ReservationWorkflow reservation={item}/>}</>}</div>
      {kind === 'invoices' && <button className="button button-ghost" onClick={()=>download(item.invoiceId)}>Descargar comprobante académico</button>}
      {item.amount != null && <strong>{money.format(Number(item.amount))}</strong>}
    </article>)}
  </section>
}
