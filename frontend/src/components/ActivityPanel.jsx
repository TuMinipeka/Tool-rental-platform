import { ReservationWorkflow } from './ReservationWorkflow.jsx'
import { useEffect, useState } from 'react'
import { createPortal } from 'react-dom'
import { api } from '../api.js'
import { Icon } from './Icon.jsx'
import { useDialogFocus } from '../hooks/useDialogFocus.js'
import { money } from '../lib/format.js'
import { entityId } from '../lib/records.js'
import { ReservationActions } from './ReservationActions.jsx'

const contracts = {
  rentals: { title: 'Alquileres', load: () => api.listRentals(), id: 'reservationId', status: 'rentalStatus' },
  payments: { title: 'Pagos registrados', load: () => api.listPayments(), id: 'paymentId', status: 'paymentStatus' },
  invoices: { title: 'Comprobantes académicos', load: () => api.listInvoices(), id: 'invoiceId', status: 'invoiceStatus' },
  reservations: { title: 'Gestión de reservas', load: () => api.listReservations(), id: 'id', status: 'status' },
}
const paymentMethodLabels = {
  CREDIT_CARD: 'Tarjeta de crédito',
  DEBIT_CARD: 'Tarjeta débito',
  PAYPAL: 'PayPal',
  PSE: 'PSE',
  MERCADO_PAGO: 'Mercado Pago',
  HOSTED_CHECKOUT: 'Checkout alojado',
  ADMINISTRATIVE: 'Registro administrativo',
}
function InvoicePreviewDialog({preview,onClose,onDownload}){
  const dialogRef=useDialogFocus(onClose)
  return <div className="invoice-preview-backdrop" role="presentation" onMouseDown={event=>event.target===event.currentTarget&&onClose()}><section ref={dialogRef} className="invoice-preview-dialog" role="dialog" aria-modal="true" aria-labelledby="invoice-preview-title"><header><div><span>Documento PDF</span><h2 id="invoice-preview-title">{preview.number}</h2></div><button onClick={onClose} aria-label="Cerrar vista previa"><Icon name="close"/></button></header><iframe src={preview.url} title={`Vista previa del comprobante ${preview.number}`}/><footer><p>Documento académico sin validez fiscal.</p><button className="button button-dark" onClick={onDownload}><Icon name="download" size={16}/>Descargar PDF</button></footer></section></div>
}
export function ActivityPanel({ kind, supplier = false }) {
  const contract = contracts[kind]
  const [items, setItems] = useState([])
  const [state, setState] = useState('loading')
  const [error, setError] = useState('')
  const [documentBusy,setDocumentBusy] = useState(null)
  const [preview,setPreview] = useState(null)
  const [revision, setRevision] = useState(0)
  useEffect(() => {
    let active = true
    const load=()=>contract.load().then(values => { if (active) { setItems(values); setError(''); setState('ready') } })
      .catch(reason => { if (active) { setError(reason.message); setState('error') } })
    load()
    const refreshVisible=()=>{if(document.visibilityState==='visible')load()}
    window.addEventListener('focus',refreshVisible)
    document.addEventListener('visibilitychange',refreshVisible)
    return () => { active = false; window.removeEventListener('focus',refreshVisible); document.removeEventListener('visibilitychange',refreshVisible) }
  }, [contract, revision])
  useEffect(()=>()=>{if(preview?.url)URL.revokeObjectURL(preview.url)},[preview?.url])
  const openPreview=async item=>{
    const id=entityId(item.invoiceId)
    setDocumentBusy(`preview-${id}`)
    setError('')
    try{const blob=await api.invoiceDocument(id);setPreview({id,number:item.number,url:URL.createObjectURL(blob)})}
    catch(reason){setError(reason.message)}
    finally{setDocumentBusy(null)}
  }
  const download=async item=>{
    const id=entityId(item.invoiceId)
    setDocumentBusy(`download-${id}`)
    setError('')
    try{const blob=await api.invoiceDocument(id,'attachment');const url=URL.createObjectURL(blob);const link=document.createElement('a');link.href=url;link.download=`comprobante-${item.number||id}.pdf`;link.click();setTimeout(()=>URL.revokeObjectURL(url),1000)}
    catch(reason){setError(reason.message)}
    finally{setDocumentBusy(null)}
  }
  const downloadPreview=()=>{const link=document.createElement('a');link.href=preview.url;link.download=`comprobante-${preview.number||preview.id}.pdf`;link.click()}
  return <section className="operations-table" aria-label={contract.title}>
    <div className="panel-heading"><div><h2>{contract.title}</h2><p>Registros devueltos por la API para tu usuario.</p></div><button className="button button-ghost" onClick={() => { setState('loading'); setRevision(value => value + 1) }}>Actualizar</button></div>
    {kind === 'payments' && <p className="panel-state">Los pagos usan medios fijos del ambiente de prueba. El servidor registra el resultado antes de confirmar la reserva.</p>}
    {kind === 'invoices' && <p className="panel-state">Documento no válido como factura ante la DIAN. Descarga el comprobante emitido por el backend.</p>}
    {state === 'loading' && <p role="status">Cargando registros…</p>}
    {state === 'error' && <p className="form-error" role="alert">{error}</p>}
    {state === 'ready' && !items.length && <p className="panel-state">No hay registros disponibles para tu usuario.</p>}
    {state === 'ready' && items.map(item => <article key={String(entityId(item[contract.id]))}>
      <div><span className="status-pill">{item[contract.status]}</span><h3>{item.number ?? String(entityId(item[contract.id])).slice(0, 8).toUpperCase()}</h3>
      {item.period && <p>{item.period.startDate} → {item.effectiveReturnDate ?? item.period.returnDate}</p>}
      {kind === 'rentals' && item.startedAt && <p>Iniciado el {new Date(item.startedAt).toLocaleString('es-CO',{timeZone:'America/Bogota'})}</p>}
      {kind === 'payments' && <p>{paymentMethodLabels[item.paymentMethod] ?? item.paymentMethod}{item.cardNetwork ? ` · ${item.cardNetwork} •••• ${item.cardLastFour} · ${String(item.cardExpiryMonth).padStart(2,'0')}/${String(item.cardExpiryYear).slice(-2)}` : ''}</p>}
      {kind === 'invoices' && <p>Emitido el {new Date(item.issuedAt).toLocaleDateString('es-CO')} · {item.issuedCustomerName}</p>}
      {kind === 'reservations' && <><ReservationActions reservation={item} supplier={supplier} onChanged={() => setRevision(value => value + 1)}/>{supplier&&item.status==='CONFIRMED'&&<ReservationWorkflow reservation={item}/>}</>}</div>
      {kind === 'invoices' && <div className="invoice-row-actions"><button className="button button-ghost" disabled={documentBusy!==null} onClick={()=>openPreview(item)}><Icon name="eye" size={16}/>Vista previa</button><button className="button button-dark" disabled={documentBusy!==null} onClick={()=>download(item)}><Icon name="download" size={16}/>Descargar PDF</button></div>}
      {item.amount != null && <strong>{money.format(Number(item.amount))}</strong>}
    </article>)}
    {preview&&createPortal(<InvoicePreviewDialog preview={preview} onClose={()=>setPreview(null)} onDownload={downloadPreview}/>,document.body)}
  </section>
}
