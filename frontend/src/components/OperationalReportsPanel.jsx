import { useEffect, useState } from 'react'
import { api } from '../api.js'

const reports = {
  income: { title: 'Ingresos', columns: [['currency','Moneda'],['grossIncome','Ingreso bruto'],['refunds','Reembolsos'],['netIncome','Ingreso neto'],['confirmedPayments','Pagos confirmados']] },
  'most-rented-tools': { title: 'Herramientas más alquiladas', columns: [['toolName','Herramienta'],['currency','Moneda'],['reservationCount','Reservas'],['rentedDays','Días'],['bookedRevenue','Ingreso reservado']] },
  'frequent-customers': { title: 'Clientes frecuentes', columns: [['customerName','Cliente'],['currency','Moneda'],['reservationCount','Reservas'],['netIncome','Ingreso neto']] },
  availability: { title: 'Disponibilidad', columns: [['toolName','Herramienta'],['totalUnits','Unidades'],['availableUnits','Disponibles'],['reservedUnits','Reservadas'],['maintenanceUnits','Mantenimiento'],['retiredUnits','Retiradas']] },
  profitability: { title: 'Ingreso reservado por herramienta', columns: [['toolName','Herramienta'],['currency','Moneda'],['reservationCount','Reservas'],['bookedRevenue','Ingreso reservado'],['averageRevenuePerReservation','Promedio por reserva']] },
}
export function OperationalReportsPanel() {
  const today = new Date().toISOString().slice(0,10)
  const [query, setQuery] = useState({ kind: 'income', from: today.slice(0,4)+'-01-01', to: today })
  const [state, setState] = useState({ status: 'loading', rows: [], error: '' })
  useEffect(() => {
    let active = true
    api.operationalReport(query.kind,query.from,query.to).then(rows => { if(active)setState({status:'ready',rows,error:''}) })
      .catch(reason => { if(active)setState({status:'error',rows:[],error:reason.message}) })
    return () => { active=false }
  },[query])
  const definition=reports[query.kind]
  const submit=event=>{
    event.preventDefault()
    const form=new FormData(event.currentTarget)
    if(form.get('from')>form.get('to')){setState({status:'error',rows:[],error:'La fecha inicial no puede ser posterior a la final.'});return}
    setState({status:'loading',rows:[],error:''})
    setQuery({kind:form.get('kind'),from:form.get('from'),to:form.get('to')})
  }
  return <section className="operations-table" aria-label="Reportes operativos">
    <div className="panel-heading"><div><h2>Reportes operativos</h2><p>Calculados por el backend según tu rol y recursos autorizados.</p></div></div>
    <form className="report-filters" onSubmit={submit}>
      <label className="form-field"><span>Reporte</span><select name="kind" defaultValue="income">{Object.entries(reports).map(([kind,report])=><option key={kind} value={kind}>{report.title}</option>)}</select></label>
      <label className="form-field"><span>Desde</span><input name="from" type="date" required defaultValue={query.from}/></label>
      <label className="form-field"><span>Hasta</span><input name="to" type="date" required defaultValue={query.to}/></label>
      <button className="button button-dark" disabled={state.status==='loading'}>Consultar reporte</button>
    </form>
    {query.kind==='profitability'&&<p className="panel-state">El indicador representa ingreso reservado. No es margen contable: el modelo todavía no registra costos operativos.</p>}
    {state.status==='loading'&&<p role="status">Cargando reporte…</p>}
    {state.status==='error'&&<p className="form-error" role="alert">{state.error}</p>}
    {state.status==='ready'&&!state.rows.length&&<p className="panel-state">No hay datos para el periodo seleccionado.</p>}
    {state.status==='ready'&&state.rows.length>0&&<div className="report-table-wrap"><table className="report-table"><caption>{definition.title}: {query.from} → {query.to}</caption><thead><tr>{definition.columns.map(([key,label])=><th scope="col" key={key}>{label}</th>)}</tr></thead><tbody>{state.rows.map((row,index)=><tr key={row.toolId??row.customerId??row.currency??index}>{definition.columns.map(([key])=><td key={key}>{row[key]??'—'}</td>)}</tr>)}</tbody></table></div>}
  </section>
}
