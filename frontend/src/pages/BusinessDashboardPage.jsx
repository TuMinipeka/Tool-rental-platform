import { useCallback, useEffect, useMemo, useState } from 'react'
import { api } from '../api.js'
import { ActivityPanel } from '../components/ActivityPanel.jsx'
import { EmptyState } from '../components/EmptyState.jsx'
import { ErrorPage } from '../components/ErrorPage.jsx'
import { Icon } from '../components/Icon.jsx'
import { OperationalReportsPanel } from '../components/OperationalReportsPanel.jsx'
import { money } from '../lib/format.js'
import { serviceLocations } from '../lib/locations.js'
import { entityId } from '../lib/records.js'

const currentDate = new Date().toISOString().slice(0, 10)
const yearStart = `${currentDate.slice(0, 4)}-01-01`

function Metric({ label, value, note, icon }) {
  return <article className="dashboard-metric"><span><Icon name={icon} size={18}/>{label}</span><strong>{value}</strong><p>{note}</p></article>
}

function SectionTabs({ active, onChange, items }) {
  return <nav className="dashboard-tabs" aria-label="Secciones del dashboard">{items.map(([id, label, icon]) => <button key={id} className={active === id ? 'active' : ''} aria-current={active === id ? 'page' : undefined} onClick={() => onChange(id)}><Icon name={icon} size={17}/>{label}</button>)}</nav>
}

function ToolForm({ categories, editing, onCancel, onSaved, showError }) {
  const [busy, setBusy] = useState(false)
  const submit = async event => {
    event.preventDefault()
    const formElement = event.currentTarget
    const form = new FormData(formElement)
    setBusy(true)
    try {
      const values = {
        name: form.get('name').trim(),
        description: form.get('description').trim(),
        serviceCity: form.get('serviceCity'),
        dailyRate: Number(form.get('dailyRate')),
        currency: 'COP',
        published: form.get('published') === 'on',
      }
      const saved = editing
        ? await api.updateTool(entityId(editing.id), values)
        : await api.createTool({ ...values, categoryId: form.get('categoryId'), imageUrls: [] })
      onSaved(saved)
      formElement.reset()
    } catch (reason) {
      showError(reason.message)
    } finally {
      setBusy(false)
    }
  }
  return <form key={editing ? String(entityId(editing.id)) : 'new'} onSubmit={submit}>
    <div className="dashboard-form-title"><div><p>{editing ? 'Editar publicación' : 'Nueva publicación'}</p><h2>{editing ? editing.name : 'Registrar herramienta'}</h2></div>{editing && <button type="button" className="text-action" onClick={onCancel}>Cancelar</button>}</div>
    <label className="form-field"><span>Categoría</span><select name="categoryId" required={!editing} disabled={Boolean(editing)} defaultValue={editing ? String(entityId(editing.categoryId)) : ''}><option value="" disabled>Selecciona una categoría</option>{categories.map(category => <option key={String(entityId(category.id))} value={String(entityId(category.id))}>{category.name}</option>)}</select></label>
    <label className="form-field"><span>Nombre</span><input name="name" required maxLength="160" defaultValue={editing?.name || ''}/></label>
    <label className="form-field"><span>Descripción</span><textarea name="description" required maxLength="2000" defaultValue={editing?.description || ''}/></label>
    <div className="two-fields"><label className="form-field"><span>Ciudad</span><select name="serviceCity" required defaultValue={editing?.serviceCity || serviceLocations[0]}>{serviceLocations.map(city => <option key={city}>{city}</option>)}</select></label><label className="form-field"><span>Tarifa diaria COP</span><input name="dailyRate" type="number" min="0" step="1" required defaultValue={Number(editing?.dailyRate?.amount ?? 0) || ''}/></label></div>
    <label className="check-field"><input name="published" type="checkbox" defaultChecked={editing?.published ?? true}/><span>Visible en el catálogo</span></label>
    <button className="button button-lime" disabled={busy}>{busy ? 'Guardando…' : editing ? 'Guardar cambios' : 'Crear herramienta'} <Icon name="arrow" size={16}/></button>
  </form>
}

function ProviderTools({ data, setData, showError }) {
  const [editing, setEditing] = useState(null)
  const [unitMode, setUnitMode] = useState(false)
  const saveTool = saved => {
    setData(current => ({ ...current, tools: current.tools.some(tool => String(entityId(tool.id)) === String(entityId(saved.id))) ? current.tools.map(tool => String(entityId(tool.id)) === String(entityId(saved.id)) ? saved : tool) : [saved, ...current.tools] }))
    setEditing(null)
  }
  const unpublish = async tool => {
    try {
      await api.deleteTool(entityId(tool.id))
      setData(current => ({ ...current, tools: current.tools.map(item => String(entityId(item.id)) === String(entityId(tool.id)) ? { ...item, published: false } : item) }))
    } catch (reason) { showError(reason.message) }
  }
  const createUnit = async event => {
    event.preventDefault()
    const element = event.currentTarget
    const form = new FormData(element)
    try {
      const created = await api.createEquipmentUnit({ toolId: form.get('toolId'), inventoryCode: form.get('inventoryCode').trim(), serialNumber: form.get('serialNumber').trim() || null, currentCondition: form.get('currentCondition'), status: 'OPERATIONAL' })
      setData(current => ({ ...current, units: [created, ...current.units] }))
      element.reset()
      setUnitMode(false)
    } catch (reason) { showError(reason.message) }
  }
  return <div className="dashboard-management-grid">
    <section className="operations-form dashboard-editor"><div className="form-tabs"><button className={!unitMode ? 'active' : ''} onClick={() => setUnitMode(false)}>Herramienta</button><button className={unitMode ? 'active' : ''} disabled={!data.tools.length} onClick={() => setUnitMode(true)}>Unidad física</button></div>{unitMode ? <form onSubmit={createUnit}><div className="dashboard-form-title"><div><p>Inventario</p><h2>Registrar unidad</h2></div></div><label className="form-field"><span>Herramienta</span><select name="toolId" required>{data.tools.map(tool => <option key={String(entityId(tool.id))} value={String(entityId(tool.id))}>{tool.name}</option>)}</select></label><label className="form-field"><span>Código de inventario</span><input name="inventoryCode" required maxLength="120"/></label><label className="form-field"><span>Serial</span><input name="serialNumber" maxLength="160"/></label><label className="form-field"><span>Condición</span><select name="currentCondition" defaultValue="GOOD"><option value="NEW">Nueva</option><option value="GOOD">Buena</option><option value="FAIR">Aceptable</option></select></label><button className="button button-lime">Registrar unidad <Icon name="arrow" size={16}/></button></form> : <ToolForm categories={data.categories} editing={editing} onCancel={() => setEditing(null)} onSaved={saveTool} showError={showError}/>}</section>
    <section className="operations-table dashboard-list"><div className="panel-heading"><div><p className="kicker dark"><span/> Catálogo propio</p><h2>Mis herramientas</h2><p>Administra todas tus publicaciones, incluidas las que no están visibles.</p></div><strong>{data.tools.length}</strong></div>{!data.tools.length ? <div className="account-empty"><Icon name="box" size={30}/><h3>No hay herramientas registradas</h3></div> : data.tools.map(tool => <article key={String(entityId(tool.id))}><div><span className={`status-pill ${tool.published ? 'green' : 'amber'}`}>{tool.published ? 'Publicada' : 'No publicada'}</span><h3>{tool.name}</h3><p>{tool.serviceCity} · {money.format(Number(tool.dailyRate?.amount ?? 0))} por día</p></div><div className="row-actions"><button className="button button-ghost" onClick={() => { setEditing(tool); setUnitMode(false) }}>Editar</button>{tool.published && <button className="danger-action" onClick={() => unpublish(tool)}>Retirar</button>}</div></article>)}</section>
  </div>
}

function ProviderManagement({ data, reload, showError }) {
  const [editing, setEditing] = useState(null)
  const [busy, setBusy] = useState(false)
  const users = useMemo(() => new Map(data.users.map(user => [String(user.userId), user])), [data.users])
  const supplierRole = data.roles.find(role => role.code === 'SUPPLIER')
  const submit = async event => {
    event.preventDefault()
    const formElement = event.currentTarget
    const form = new FormData(formElement)
    setBusy(true)
    try {
      if (editing) {
        await api.updateSupplier(editing.userId, { userId: editing.userId, documentType: form.get('documentType'), documentNumber: form.get('documentNumber').trim(), businessName: form.get('businessName').trim() })
      } else {
        const created = await api.createUser({ email: form.get('email').trim(), displayName: form.get('displayName').trim(), password: form.get('password') })
        await api.assignUserRole(created.userId, supplierRole.roleId)
        await api.createSupplier({ userId: created.userId, documentType: form.get('documentType'), documentNumber: form.get('documentNumber').trim(), businessName: form.get('businessName').trim() })
      }
      setEditing(null)
      formElement.reset()
      await reload()
    } catch (reason) { showError(reason.message) } finally { setBusy(false) }
  }
  const deactivate = async supplier => {
    try { await api.deleteSupplier(supplier.userId); await reload() } catch (reason) { showError(reason.message) }
  }
  return <div className="dashboard-management-grid"><section className="operations-form dashboard-editor"><form key={editing?.userId || 'new-provider'} onSubmit={submit}><div className="dashboard-form-title"><div><p>{editing ? 'Editar proveedor' : 'Nuevo proveedor'}</p><h2>{editing ? editing.businessName : 'Crear cuenta'}</h2></div>{editing && <button type="button" className="text-action" onClick={() => setEditing(null)}>Cancelar</button>}</div>{!editing && <><label className="form-field"><span>Nombre responsable</span><input name="displayName" required maxLength="200"/></label><label className="form-field"><span>Correo</span><input name="email" type="email" required maxLength="320"/><small>El proveedor podrá editar estos datos desde Mi cuenta.</small></label><label className="form-field"><span>Contraseña inicial</span><input name="password" type="password" required minLength="12"/></label></>}<label className="form-field"><span>Razón social</span><input name="businessName" required maxLength="200" defaultValue={editing?.businessName || ''}/></label><div className="two-fields"><label className="form-field"><span>Tipo de documento</span><select name="documentType" defaultValue={editing?.documentType || 'NIT'}><option>NIT</option><option>CC</option><option>CE</option><option>PASSPORT</option></select></label><label className="form-field"><span>Número</span><input name="documentNumber" required maxLength="40" defaultValue={editing?.documentNumber || ''}/></label></div><button className="button button-lime" disabled={busy || (!editing && !supplierRole)}>{busy ? 'Guardando…' : editing ? 'Guardar proveedor' : 'Crear proveedor'} <Icon name="arrow" size={16}/></button></form></section><section className="operations-table dashboard-list"><div className="panel-heading"><div><p className="kicker dark"><span/> Gestión de acceso</p><h2>Proveedores</h2></div><strong>{data.suppliers.length}</strong></div>{data.suppliers.map(supplier => { const user = users.get(String(supplier.userId)); return <article key={supplier.userId}><div><span className={`status-pill ${user?.accountStatus === 'ACTIVE' ? 'green' : 'amber'}`}>{user?.accountStatus || 'SIN CUENTA'}</span><h3>{supplier.businessName}</h3><p>{user?.displayName} · {user?.email} · {supplier.documentType} {supplier.documentNumber}</p></div><div className="row-actions"><button className="button button-ghost" onClick={() => setEditing(supplier)}>Editar</button>{user?.accountStatus === 'ACTIVE' && <button className="danger-action" onClick={() => deactivate(supplier)}>Desactivar</button>}</div></article> })}</section></div>
}

function AdministratorManagement({ data, session, reload, showError }) {
  const [editing, setEditing] = useState(null)
  const [busy, setBusy] = useState(false)
  const adminRole = data.roles.find(role => role.code === 'ADMIN')
  const adminIds = new Set(data.userRoles.filter(entry => Number(entry.roleId) === Number(adminRole?.roleId)).map(entry => String(entry.userId)))
  const administrators = data.users.filter(user => adminIds.has(String(user.userId)))
  const submit = async event => {
    event.preventDefault()
    const formElement = event.currentTarget
    const form = new FormData(formElement)
    setBusy(true)
    try {
      if (editing) await api.updateUser(editing.userId, { displayName: form.get('displayName').trim(), email: form.get('email').trim() })
      else {
        const created = await api.createUser({ displayName: form.get('displayName').trim(), email: form.get('email').trim(), password: form.get('password') })
        await api.assignUserRole(created.userId, adminRole.roleId)
      }
      setEditing(null)
      formElement.reset()
      await reload()
    } catch (reason) { showError(reason.message) } finally { setBusy(false) }
  }
  const deactivate = async administrator => {
    try { await api.updateUserStatus(administrator.userId, 'DISABLED'); await reload() } catch (reason) { showError(reason.message) }
  }
  return <div className="dashboard-management-grid"><section className="operations-form dashboard-editor"><form key={editing?.userId || 'new-admin'} onSubmit={submit}><div className="dashboard-form-title"><div><p>{editing ? 'Editar administrador' : 'Nuevo administrador'}</p><h2>{editing ? editing.displayName : 'Crear acceso'}</h2></div>{editing && <button type="button" className="text-action" onClick={() => setEditing(null)}>Cancelar</button>}</div><label className="form-field"><span>Nombre completo</span><input name="displayName" required maxLength="200" defaultValue={editing?.displayName || ''}/></label><label className="form-field"><span>Correo</span><input name="email" type="email" required maxLength="320" defaultValue={editing?.email || ''}/></label>{!editing && <label className="form-field"><span>Contraseña inicial</span><input name="password" type="password" required minLength="12"/></label>}<button className="button button-lime" disabled={busy || !adminRole}>{busy ? 'Guardando…' : editing ? 'Guardar administrador' : 'Crear administrador'} <Icon name="arrow" size={16}/></button></form></section><section className="operations-table dashboard-list"><div className="panel-heading"><div><p className="kicker dark"><span/> Control de plataforma</p><h2>Administradores</h2></div><strong>{administrators.length}</strong></div>{administrators.map(administrator => <article key={administrator.userId}><div><span className={`status-pill ${administrator.accountStatus === 'ACTIVE' ? 'green' : 'amber'}`}>{administrator.accountStatus}</span><h3>{administrator.displayName}</h3><p>{administrator.email}{String(administrator.userId) === String(session.userId) ? ' · Sesión actual' : ''}</p></div><div className="row-actions">{String(administrator.userId) !== String(session.userId) && <><button className="button button-ghost" onClick={() => setEditing(administrator)}>Editar</button>{administrator.accountStatus === 'ACTIVE' && <button className="danger-action" onClick={() => deactivate(administrator)}>Desactivar</button>}</>}</div></article>)}</section></div>
}

export function BusinessDashboardPage({ role, session }) {
  const userId = session?.userId
  const provider = role === 'provider'
  const authorized = Boolean(session?.roles?.includes(provider ? 'SUPPLIER' : 'ADMIN'))
  const [data, setData] = useState({ tools: [], units: [], categories: [], users: [], suppliers: [], roles: [], userRoles: [] })
  const [income, setIncome] = useState(null)
  const [state, setState] = useState('loading')
  const [error, setError] = useState('')
  const [active, setActive] = useState('summary')

  const reload = useCallback(async () => {
    setError('')
    const common = [api.listTools(), api.listEquipmentUnits(), api.listCategories(), api.operationalReport('income', yearStart, currentDate)]
    const results = provider ? await Promise.all(common) : await Promise.all([...common, api.listUsers(), api.listSuppliers(), api.listRoles(), api.listUserRoles()])
    const [allTools, units, categories, incomeRows, users = [], suppliers = [], roles = [], userRoles = []] = results
    const tools = provider ? allTools.filter(tool => String(entityId(tool.supplierId)) === String(userId)) : allTools
    setData({ tools, units, categories, users, suppliers, roles, userRoles })
    setIncome(Array.isArray(incomeRows) ? incomeRows.find(row => row.currency === 'COP') || incomeRows[0] || null : null)
    setState('ready')
  }, [provider, userId])

  useEffect(() => {
    if (!authorized) return undefined
    let activeRequest = true
    const load = async () => { try { await reload() } catch (reason) { if (activeRequest) { setError(reason.message); setState('error') } } }
    load()
    return () => { activeRequest = false }
  }, [authorized, reload])

  if (!session) return <EmptyState title="Acceso restringido" text="Ingresa con una cuenta autorizada para abrir este espacio." action={() => window.dispatchEvent(new Event('open-auth'))} actionText="Ingresar"/>
  if (!authorized) return <ErrorPage code="403"/>

  const operational = data.units.filter(unit => unit.status === 'OPERATIONAL').length
  const activeUsers = data.users.filter(user => user.accountStatus === 'ACTIVE').length
  const tabs = provider ? [['summary', 'Resumen', 'grid'], ['tools', 'Herramientas', 'box']] : [['summary', 'Resumen', 'grid'], ['providers', 'Proveedores', 'box'], ['administrators', 'Administradores', 'shield']]
  return <main className="operations-page page-shell page-top staff-dashboard"><header className="operations-heading"><p className="kicker dark"><span/> {provider ? 'Dashboard de proveedor' : 'Dashboard administrativo'}</p><h1>{provider ? 'Tu operación, en un solo lugar.' : 'El pulso de la plataforma.'}</h1><p>{provider ? 'Publica, edita y retira tus herramientas. Los ingresos y reservas corresponden únicamente a tu operación.' : 'Consulta ingresos registrados, actividad y cuentas autorizadas con información persistida por el sistema.'}</p></header><SectionTabs active={active} onChange={setActive} items={tabs}/>{error && <div className="form-error" role="alert">{error}</div>}{state === 'loading' && <div className="integration-banner"><Icon name="grid"/><div><strong>Cargando dashboard</strong><p>Consultando información autorizada.</p></div></div>}{state === 'ready' && <>{active === 'summary' && <><section className="dashboard-metrics"><Metric icon="wallet" label="Ingreso neto" value={money.format(Number(income?.netIncome ?? 0))} note="Ingresos confirmados menos reembolsos"/><Metric icon="card" label="Ingreso bruto" value={money.format(Number(income?.grossIncome ?? 0))} note="No equivale a utilidad contable"/><Metric icon="check" label="Pagos confirmados" value={Number(income?.confirmedPayments ?? 0)} note={`Periodo ${yearStart} a ${currentDate}`}/><Metric icon={provider ? 'box' : 'user'} label={provider ? 'Unidades operativas' : 'Cuentas activas'} value={provider ? operational : activeUsers} note={provider ? `${data.tools.length} herramientas propias` : `${data.users.length} cuentas registradas`}/></section><OperationalReportsPanel/><ActivityPanel kind="reservations" supplier={provider}/></>}{provider && active === 'tools' && <ProviderTools data={data} setData={setData} showError={setError}/>} {!provider && active === 'providers' && <ProviderManagement data={data} reload={reload} showError={setError}/>} {!provider && active === 'administrators' && <AdministratorManagement data={data} session={session} reload={reload} showError={setError}/>}</>}</main>
}
