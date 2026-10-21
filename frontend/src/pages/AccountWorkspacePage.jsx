import { useEffect, useState } from 'react'
import { api, saveSession } from '../api.js'
import { ActivityPanel } from '../components/ActivityPanel.jsx'
import { EmptyState } from '../components/EmptyState.jsx'
import { Icon } from '../components/Icon.jsx'
import { ReservationActions } from '../components/ReservationActions.jsx'
import { money } from '../lib/format.js'
import { navigate } from '../lib/navigation.js'
import { reservationValue } from '../lib/records.js'

function ProfileSecurityPanel({session,onLogout}){
  const [profile,setProfile]=useState({displayName:session.displayName||'',email:session.email||''})
  const [profileState,setProfileState]=useState('loading')
  const [busy,setBusy]=useState('')
  const [message,setMessage]=useState('')
  const [error,setError]=useState('')

  useEffect(()=>{
    let active=true
    api.currentUser().then(user=>{if(active){setProfile({displayName:user.displayName,email:user.email});setProfileState('ready')}}).catch(reason=>{if(active){setError(reason.message);setProfileState('error')}})
    return()=>{active=false}
  },[session.userId])

  const updateProfile=async event=>{
    event.preventDefault()
    setBusy('profile');setError('');setMessage('')
    try{
      const updated=await api.updateCurrentUser(profile)
      setProfile({displayName:updated.displayName,email:updated.email})
      saveSession({...session,displayName:updated.displayName,email:updated.email})
      setMessage('Tus datos se actualizaron correctamente.')
    }catch(reason){setError(reason.message)}finally{setBusy('')}
  }

  const updatePassword=async event=>{
    event.preventDefault()
    const formElement=event.currentTarget
    const form=new FormData(formElement)
    setError('');setMessage('')
    if(form.get('newPassword')!==form.get('confirmPassword')){setError('Las contraseñas nuevas no coinciden.');return}
    setBusy('password')
    try{
      await api.changePassword({currentPassword:form.get('currentPassword'),newPassword:form.get('newPassword')})
      formElement.reset()
      await onLogout('/login')
    }catch(reason){setError(reason.message);setBusy('')}
  }

  return <div className="profile-security"><header><p className="kicker dark"><span/> Perfil y seguridad</p><h2>Administra tu cuenta</h2><p>Actualiza tus datos o cambia la contraseña usando tu clave actual.</p></header>{error&&<div className="form-error" role="alert">{error}</div>}{message&&<div className="account-success" role="status"><Icon name="check"/><span>{message}</span></div>}<div className="profile-security-grid"><form onSubmit={updateProfile} aria-busy={profileState==='loading'}><div className="profile-form-heading"><span><Icon name="user"/></span><div><h3>Información personal</h3><p>{profileState==='loading'?'Cargando los datos guardados…':'Estos datos se muestran en tu sesión.'}</p></div></div><label className="form-field"><span>Nombre completo</span><input value={profile.displayName} disabled={profileState!=='ready'||busy!==''} onChange={event=>setProfile(current=>({...current,displayName:event.target.value}))} required maxLength="200" autoComplete="name"/></label><label className="form-field"><span>Correo electrónico</span><input type="email" value={profile.email} disabled={profileState!=='ready'||busy!==''} onChange={event=>setProfile(current=>({...current,email:event.target.value}))} required maxLength="320" autoComplete="email"/></label><button className="button button-dark" disabled={profileState!=='ready'||busy!==''}>{busy==='profile'?'Guardando…':'Guardar cambios'} <Icon name="arrow" size={16}/></button></form><form onSubmit={updatePassword}><div className="profile-form-heading"><span><Icon name="shield"/></span><div><h3>Cambiar contraseña</h3><p>Al guardarla se cerrarán todas las sesiones.</p></div></div><label className="form-field"><span>Contraseña actual</span><input name="currentPassword" type="password" required minLength="12" autoComplete="current-password"/></label><label className="form-field"><span>Nueva contraseña</span><input name="newPassword" type="password" required minLength="12" autoComplete="new-password"/></label><label className="form-field"><span>Confirmar contraseña</span><input name="confirmPassword" type="password" required minLength="12" autoComplete="new-password"/></label><button className="button button-dark" disabled={busy!==''}>{busy==='password'?'Actualizando…':'Actualizar contraseña'} <Icon name="arrow" size={16}/></button></form></div></div>
}

export function AccountWorkspacePage({session,onLogout}){
  const userId=session?.userId
  const staffRole=session?.roles?.includes('ADMIN')?'ADMIN':session?.roles?.includes('SUPPLIER')?'SUPPLIER':null
  const [reservations,setReservations]=useState([])
  const [state,setState]=useState(()=>session?'loading':'idle')
  const [error,setError]=useState('')
  const [activeSection,setActiveSection]=useState('reservations')

  const refreshReservations=async(showLoading=false)=>{
    if(showLoading)setState('loading')
    try{const items=await api.listReservations();setReservations(Array.isArray(items)?items:[]);setError('');setState('ready')}
    catch(reason){setError(reason.message);setState('error')}
  }

  useEffect(()=>{
    if(!userId||staffRole)return undefined
    let active=true
    api.listReservations().then(items=>{if(active){setReservations(Array.isArray(items)?items:[]);setState('ready')}}).catch(reason=>{if(active){setError(reason.message);setState('error')}})
    const refresh=()=>{if(active&&document.visibilityState==='visible')refreshReservations()}
    window.addEventListener('focus',refresh)
    document.addEventListener('visibilitychange',refresh)
    return()=>{active=false;window.removeEventListener('focus',refresh);document.removeEventListener('visibilitychange',refresh)}
  },[userId,staffRole])

  if(!session)return <EmptyState title="Ingresa para ver tu cuenta" text="Consulta aquí tus reservas y el estado real de tu operación." action={()=>window.dispatchEvent(new Event('open-auth'))} actionText="Ingresar"/>

  const name=session.displayName||session.email
  if(staffRole){
    const dashboardPath=staffRole==='ADMIN'?'/panel/admin':'/panel/proveedor'
    return <main className="staff-account-page page-shell page-top"><header className="staff-account-heading"><div><p className="kicker dark"><span/> Mi cuenta</p><h1>Perfil y seguridad.</h1><p>Este espacio contiene únicamente la administración de tu cuenta. La operación permanece en el dashboard.</p></div><button className="button button-dark" onClick={()=>navigate(dashboardPath)}>Volver al dashboard <Icon name="arrow" size={17}/></button></header><div className="staff-account-layout"><aside className="profile-card"><span className="profile-avatar">{name.slice(0,1).toUpperCase()}</span><h2>{name}</h2><p>{session.email}</p><span className="role-badge">{staffRole==='ADMIN'?'Administrador':'Proveedor'}</span><div><button onClick={()=>onLogout()}><Icon name="logout"/>Cerrar sesión <Icon name="chevron"/></button></div></aside><section className="account-panel"><ProfileSecurityPanel session={session} onLogout={onLogout}/></section></div></main>
  }
  const pending=reservations.filter(item=>String(item.status).includes('PENDING')).length
  const modules=[['reservations','calendar','Mis reservas','Gestiona pagos, cancelaciones y estados.'],['rentals','box','Alquileres','Consulta el avance de cada alquiler.'],['payments','card','Pagos','Revisa medios, importes y resultados.'],['invoices','file','Comprobantes','Vista previa y descarga en PDF.'],['security','shield','Perfil y seguridad','Edita tus datos y contraseña.']]

  return <main className="account-page"><section className="account-hero"><div className="page-shell"><div><p className="kicker"><span/> Mi perfil</p><h1>Tu perfil, tus alquileres,<br/><em>todo en un solo lugar.</em></h1><p>Los paneles se actualizan con la información persistida de tu cuenta.</p><button className="button button-lime" onClick={()=>navigate('/catalogo')}>Explorar herramientas <Icon name="arrow"/></button></div><img src="/assets/hero-rotary-hammer-1200.png" alt=""/></div></section><div className="account-layout page-shell"><aside className="profile-card"><span className="profile-avatar">{name.slice(0,1).toUpperCase()}</span><h2>{name}</h2><p>{session.email}</p><span className="role-badge">{session.roles?.join(' · ')||'Cuenta autenticada'}</span><div><button onClick={()=>setActiveSection('security')}><Icon name="user"/>Editar perfil <Icon name="chevron"/></button><button onClick={()=>onLogout()}><Icon name="logout"/>Cerrar sesión <Icon name="chevron"/></button></div></aside><section className="account-workspace"><div className="account-modules">{modules.map(([id,icon,title,copy])=><button key={id} className={activeSection===id?'active':''} onClick={()=>setActiveSection(id)}><span><Icon name={icon}/></span><div><strong>{title}</strong><p>{copy}</p></div><Icon name="arrow"/></button>)}</div><div className="account-panel" aria-live="polite">{activeSection==='reservations'?<><div className="panel-heading"><div><p className="kicker dark"><span/> Actividad actual</p><h2>Mis reservas</h2></div><div><span className="status-pill amber">{pending} pendientes</span><button className="button button-ghost" onClick={()=>refreshReservations(true)}>Actualizar reservas</button></div></div>{state==='loading'&&<p className="panel-state">Cargando tus reservas…</p>}{state==='error'&&<div className="form-error" role="alert">{error}</div>}{state==='ready'&&!reservations.length&&<div className="account-empty"><Icon name="calendar" size={34}/><h3>Aún no tienes reservas</h3><p>Cuando crees una solicitud desde el catálogo, aparecerá aquí.</p><button className="button button-dark" onClick={()=>navigate('/catalogo')}>Explorar catálogo</button></div>}{state==='ready'&&reservations.map(item=><article className="reservation-row" key={String(reservationValue(item,'id'))}><div><span className={`status-pill ${item.status==='CONFIRMED'?'green':'amber'}`}>{String(item.status).replaceAll('_',' ')}</span><h3>Reserva {String(reservationValue(item,'id')).slice(0,8).toUpperCase()}</h3><p>{item.period?.startDate} → {item.effectiveReturnDate||item.period?.returnDate}</p>{item.status==='PENDING_PAYMENT'&&item.expiresAt&&<p>Retención hasta {new Date(item.expiresAt).toLocaleString('es-CO',{timeZone:'America/Bogota'})}</p>}</div><div><small>Total registrado</small><strong>{money.format(Number(item.total?.amount??0))}</strong><ReservationActions reservation={item} onChanged={()=>refreshReservations()}/></div></article>)}</>:activeSection==='security'?<ProfileSecurityPanel session={session} onLogout={onLogout}/>:<ActivityPanel key={activeSection} kind={activeSection}/>}</div></section></div></main>
}
