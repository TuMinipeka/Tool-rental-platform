import { useState } from 'react'
import { api } from '../api.js'
import { entityId } from '../lib/records.js'

export function EquipmentUnitManager({ units, tools, onChanged }) {
  const [editing, setEditing] = useState(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const submit = async event => {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    setBusy(true); setError('')
    try {
      const saved = await api.updateEquipmentUnit(entityId(editing.id), { serialNumber: form.get('serialNumber').trim() || null, currentCondition: form.get('currentCondition'), status: form.get('status') })
      onChanged(saved); setEditing(null)
    } catch (reason) { setError(reason.message) } finally { setBusy(false) }
  }
  return <section className="equipment-unit-manager" aria-label="Unidades físicas registradas"><h2>Unidades físicas registradas</h2>{error && <p className="form-error" role="alert">{error}</p>}
    {!units.length && <p>No hay unidades físicas registradas.</p>}
    {units.map(unit => <article key={String(entityId(unit.id))}><div><h3>{unit.inventoryCode}</h3><p>{tools.find(tool => String(entityId(tool.id)) === String(entityId(unit.toolId)))?.name} · {unit.status}</p></div>{unit.status !== 'RETIRED' && <button type="button" className="button button-ghost" onClick={() => setEditing(unit)}>Editar unidad</button>}</article>)}
    {editing && <form key={String(entityId(editing.id))} onSubmit={submit} aria-label={`Editar unidad ${editing.inventoryCode}`}><label className="form-field"><span>Serial</span><input name="serialNumber" defaultValue={editing.serialNumber || ''} maxLength="160"/></label><label className="form-field"><span>Condición</span><select name="currentCondition" defaultValue={editing.currentCondition}><option value="NEW">Nueva</option><option value="GOOD">Buena</option><option value="FAIR">Aceptable</option></select></label><label className="form-field"><span>Estado operativo</span><select name="status" defaultValue={editing.status}><option value="OPERATIONAL">Operativa</option><option value="MAINTENANCE">En mantenimiento</option><option value="RETIRED">Retirada definitivamente</option></select></label><p>Retirar definitivamente una unidad no permite reactivarla.</p><button className="button button-dark" disabled={busy}>Guardar unidad</button><button type="button" className="button button-ghost" disabled={busy} onClick={() => setEditing(null)}>Cancelar edición</button></form>}
  </section>
}
