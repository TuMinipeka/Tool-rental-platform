import { useCallback, useEffect, useState } from 'react'
import { api } from '../api.js'
import { entityId } from '../lib/records.js'

const optional = promise => promise.catch(error => { if(error.status===404)return null; throw error })
function Workflow({ reservation }) {
  const id=entityId(reservation.id)
  const [data,setData]=useState(null)
  const [error,setError]=useState('')
  const [busy,setBusy]=useState(false)
  const load=useCallback(async()=>{
    const [rental,entries]=await Promise.all([
      optional(api.getRental(id)),
      Promise.all(reservation.items.map(async item=>{
        const unitId=entityId(item.equipmentUnitId)
        const [delivery,returned]=await Promise.all([optional(api.deliveryEntry(id,unitId)),optional(api.returnEntry(id,unitId))])
        return {unitId,delivery,returned}
      })),
    ])
    return {rental,entries}
  },[id,reservation.items])
  useEffect(()=>{
    let active=true
    load().then(value=>{if(active)setData(value)}).catch(reason=>{if(active)setError(reason.message)})
    return()=>{active=false}
  },[load])
  const perform=async operation=>{
    setBusy(true);setError('')
    try{await operation();setData(await load())}catch(reason){setError(reason.message)}finally{setBusy(false)}
  }
  if(!data)return <div>{error?<p role="alert" className="form-error">{error}</p>:<p role="status">Consultando entregas y alquiler…</p>}</div>
  return <div className="rental-workflow" aria-busy={busy}>
    <p>Alquiler: <strong>{data.rental?.rentalStatus??'Sin iniciar'}</strong>. El servidor valida cada transición.</p>
    {error&&<p role="alert" className="form-error">{error}</p>}
    {data.entries.map(({unitId,delivery,returned})=><section key={unitId} className="workflow-unit" aria-label={'Unidad '+unitId.slice(-8)}>
      <h4>Unidad {unitId.slice(-8)}</h4>
      <p>Entrega: {delivery?.deliveryStatus??'Sin programar'} · Devolución: {returned?'Registrada':'Pendiente'}</p>{returned&&<p>Mora registrada: {returned.lateDays} días · {returned.lateFee?.amount} {returned.lateFee?.currency}</p>}
      {!delivery&&<form className="report-filters" onSubmit={event=>{
        event.preventDefault();const fields=Object.fromEntries(new FormData(event.currentTarget))
        perform(()=>api.scheduleDelivery({reservationId:id,equipmentUnitId:unitId,scheduledAt:reservation.scheduledFulfillmentAt,outboundCondition:'GOOD',country:'CO',...fields}))
      }}>
        <label className="form-field"><span>Dirección de entrega o recogida</span><input name="addressLine" required maxLength={300}/></label>
        <label className="form-field"><span>Ciudad</span><input name="city" required maxLength={120}/></label>
        <label className="form-field"><span>Departamento</span><input name="region" required maxLength={120}/></label>
        <button className="button button-dark" disabled={busy}>Programar entrega</button>
      </form>}
      {delivery?.deliveryStatus==='SCHEDULED'&&<form className="report-filters" onSubmit={event=>{event.preventDefault();const condition=new FormData(event.currentTarget).get('condition');perform(()=>api.completeDelivery(id,unitId,condition))}}>
        <label className="form-field"><span>Condición de salida</span><input name="condition" required maxLength={40} defaultValue="GOOD"/></label>
        <button className="button button-dark" disabled={busy}>Completar entrega</button>
      </form>}
      {data.rental?.rentalStatus==='ACTIVE'&&!returned&&<form className="report-filters" onSubmit={event=>{event.preventDefault();const fields=Object.fromEntries(new FormData(event.currentTarget));perform(()=>api.returnUnit({reservationId:id,equipmentUnitId:unitId,...fields}))}}>
        <label className="form-field"><span>Condición de devolución</span><input name="inboundCondition" required maxLength={40} defaultValue="GOOD"/></label>
        <label className="form-field"><span>Notas de devolución</span><input name="notes" maxLength={500}/></label>
        <button className="button button-dark" disabled={busy}>Registrar devolución</button>
      </form>}
    </section>)}
    {!data.rental&&<button className="button button-dark" disabled={busy||data.entries.some(item=>item.delivery?.deliveryStatus!=='COMPLETED')} onClick={()=>perform(()=>api.startRental(id))}>Iniciar alquiler</button>}
    {data.rental?.rentalStatus==='ACTIVE'&&<button className="button button-dark" disabled={busy||data.entries.some(item=>!item.returned)} onClick={()=>perform(()=>api.completeRental(id))}>Cerrar alquiler</button>}
  </div>
}
export function ReservationWorkflow({ reservation }) {
  const [open,setOpen]=useState(false)
  return <details className="reservation-workflow" onToggle={event=>setOpen(event.currentTarget.open)}><summary>Entrega y devolución</summary>{open&&<Workflow key={String(entityId(reservation.id))} reservation={reservation}/>}</details>
}
