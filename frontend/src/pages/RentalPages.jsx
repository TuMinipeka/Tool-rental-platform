import { demoCatalog as tools } from '../demoCatalog.js'
import { useEffect, useRef, useState } from 'react'
import { api } from '../api.js'
import { Icon } from '../components/Icon.jsx'
import { ToolVisual } from '../components/ToolVisual.jsx'
import { EmptyState } from '../components/EmptyState.jsx'
import { useFeatures } from '../lib/features.js'
import { navigate } from '../lib/navigation.js'
import { money } from '../lib/format.js'
import { serviceLocations } from '../lib/locations.js'
import { entityId } from '../lib/records.js'

export function QuotePage({reservation,session,onContinue}){
  const [fulfillmentMethod,setFulfillmentMethod]=useState('CUSTOMER_PICKUP')
  const [scheduledFulfillmentAt,setScheduledFulfillmentAt]=useState('')
  const [deliveryAddressLine,setDeliveryAddressLine]=useState('')
  const [deliveryCity,setDeliveryCity]=useState('Bucaramanga')
  const deliveryRegion='Santander'
  const [submitting,setSubmitting]=useState(false)

  if(!reservation)return <EmptyState title="Tu cotización está vacía" text="Selecciona primero una herramienta y las fechas de uso." action={()=>navigate('/catalogo')} actionText="Explorar catálogo"/>

  const subtotal=reservation.tool.price*reservation.days
  const deliveryRequired=fulfillmentMethod==='SUPPLIER_DELIVERY'
  const logisticsReady=scheduledFulfillmentAt&&(!deliveryRequired||(deliveryAddressLine&&deliveryCity&&deliveryRegion))
  const reservationReady=Array.isArray(reservation.equipmentUnitIds)&&reservation.equipmentUnitIds.length>0&&!reservation.availabilityError
  const continueCheckout=async()=>{
    if(submitting)return
    setSubmitting(true)
    try{
      await onContinue({
        fulfillmentMethod,
        scheduledFulfillmentAt,
        deliveryAddressLine:deliveryRequired?deliveryAddressLine:null,
        deliveryCity:deliveryRequired?deliveryCity:null,
        deliveryRegion:deliveryRequired?deliveryRegion:null,
        deliveryCountryCode:deliveryRequired?'CO':null,
      })
    }finally{setSubmitting(false)}
  }

  return <main className="checkout-page page-shell page-top"><div className="checkout-steps"><span className="done">1</span><i/><span className="active">2</span><i/><span>3</span><small>Herramienta</small><small>Solicitud</small><small>Pago</small></div><div className="checkout-layout"><section><p className="kicker dark"><span/> Revisa antes de continuar</p><h1>Tu cotización.</h1><div className="quote-tool"><ToolVisual tool={reservation.tool} compact/><div><small>{reservation.tool.category}</small><h2>{reservation.tool.name}</h2><p><Icon name="calendar" size={17}/>{reservation.startDate} → {reservation.returnDate}</p><p><Icon name="pin" size={17}/>{reservation.tool.location}</p></div></div><div className="logistics-card"><h2>Entrega o recogida</h2><p>Define cómo y cuándo esperas recibir el equipo antes de crear la reserva.</p><div className="fulfillment-options"><button className={fulfillmentMethod==='CUSTOMER_PICKUP'?'active':''} onClick={()=>setFulfillmentMethod('CUSTOMER_PICKUP')}><Icon name="pin"/><span><strong>Recoger con el proveedor</strong><small>Coordina el punto después de completar el pago.</small></span></button><button className={deliveryRequired?'active':''} onClick={()=>setFulfillmentMethod('SUPPLIER_DELIVERY')}><Icon name="box"/><span><strong>Entrega del proveedor</strong><small>La tarifa versionada se calcula en el backend.</small></span></button></div><label className="form-field"><span>Fecha y hora programada</span><input type="datetime-local" min={`${reservation.startDate}T00:00`} value={scheduledFulfillmentAt} onChange={event=>setScheduledFulfillmentAt(event.target.value)} required/></label>{deliveryRequired&&<div className="delivery-fields"><label className="form-field"><span>Dirección de entrega</span><input value={deliveryAddressLine} onChange={event=>setDeliveryAddressLine(event.target.value)} placeholder="Calle, número y complemento" required/></label><div className="two-fields"><label className="form-field"><span>Ciudad</span><select value={deliveryCity} onChange={event=>setDeliveryCity(event.target.value)} required>{serviceLocations.map(city=><option key={city} value={city}>{city}</option>)}</select></label><label className="form-field"><span>Departamento</span><input value={deliveryRegion} readOnly aria-readonly="true"/></label></div></div>}</div><div className="notice-card"><Icon name="shield"/><div><strong>Precio base, sin cálculo tributario definitivo</strong><p>La plataforma todavía no aplica impuestos ni protección. La entrega se cotiza con la configuración versionada del backend.</p></div></div>{reservation.availabilityError?<div className="form-error" role="alert">{reservation.availabilityError}</div>:null}{reservation.apiError?<div className="form-error" role="alert">{reservation.apiError}</div>:null}{!session&&<div className="auth-nudge"><h3>Ingresa para crear la solicitud</h3><p>Conservaremos esta cotización mientras completas el acceso.</p></div>}</section><aside className="price-card"><h2>Resumen</h2><div><span>Tarifa diaria</span><strong>{money.format(reservation.tool.price)}</strong><span>Días facturables</span><strong>{reservation.days}</strong><span>Subtotal alquiler</span><strong>{money.format(subtotal)}</strong></div><div className="split-payment"><small>Importe validado por el servidor</small><p><span>Alquiler estimado</span><strong>{money.format(subtotal)}</strong></p><p><span>Total final</span><strong>Lo confirma el servidor</strong></p></div><div className="price-total"><span>Total base estimado</span><strong>{money.format(subtotal)}</strong></div><button className="button button-dark wide" disabled={Boolean(session)&&(!logisticsReady||!reservationReady||submitting)} onClick={continueCheckout}>{submitting?'Creando reserva…':session?'Crear reserva y pagar':'Ingresar y continuar'} <Icon name="arrow"/></button><button className="button button-ghost wide" onClick={()=>navigate(`/herramienta/${reservation.tool.id}`)}>Editar fechas</button><p className="checkout-legal">Al continuar podrás revisar los <button onClick={()=>navigate('/terms-and-conditions')}>Términos y Condiciones</button>. El pago se inicia únicamente después de crear la reserva en el servidor.</p></aside></div></main>}

export function SuccessPage({reservation}){if(!reservation)return <EmptyState title="Consulta el estado de tu orden" text="Abre tu cuenta para consultar las reservas persistidas." action={()=>navigate('/cuenta')} actionText="Abrir mi cuenta"/>;const reservationCode=reservation?.reservationId?`TR-${String(reservation.reservationId).slice(0,8).toUpperCase()}`:'TR-DEMO';return <main className="success-page page-shell page-top"><div className="success-mark"><Icon name="check" size={42}/></div><p className="kicker dark"><span/> Solicitud creada</p><h1>Tu herramienta está<br/>un paso más cerca.</h1><p>La orden retiene temporalmente la unidad. Abre tus reservas para continuar con el pago. La reserva se confirma después de verificar el resultado.</p><div className="success-ticket"><div><small>Código</small><strong>{reservationCode}</strong></div><div><small>Equipo</small><strong>{reservation?.tool.name||tools[0].name}</strong></div><div><small>Estado</small><strong>{reservation.reservationStatus||'PENDING_PAYMENT'}</strong></div></div><p className="success-legal">Consulta los <button onClick={()=>navigate('/terms-and-conditions')}>Términos y Condiciones</button> aplicables al ambiente de prueba.</p><div className="success-actions"><button className="button button-dark" onClick={()=>navigate('/cuenta')}>Ver mis reservas <Icon name="arrow"/></button><button className="button button-ghost" onClick={()=>navigate('/catalogo')}>Seguir explorando</button></div></main>}

const isAllowedMercadoPagoUrl = value => {
  const target = new URL(value, window.location.origin)
  const hostAllowed = ['mercadopago.com', 'mercadopago.com.co'].some(host => target.hostname === host || target.hostname.endsWith(`.${host}`))
  return target.protocol === 'https:' && hostAllowed ? target : null
}

const fixedPaymentMethods = [
  {
    id: 'visa',
    label: 'Visa',
    subtitle: 'Tarjeta de crédito',
    icon: 'card',
    paymentMethod: 'CREDIT_CARD',
    cardNetwork: 'VISA',
    cardLastFour: '1111',
    cardExpiryMonth: 12,
    cardExpiryYear: 2030,
    fields: [['Número', '4111 1111 1111 1111'], ['Titular', 'CLIENTE TOOL RENTAL'], ['Vencimiento', '12/30'], ['CVV', '123']],
  },
  {
    id: 'mastercard',
    label: 'Mastercard',
    subtitle: 'Tarjeta de crédito',
    icon: 'card',
    paymentMethod: 'CREDIT_CARD',
    cardNetwork: 'MASTERCARD',
    cardLastFour: '4444',
    cardExpiryMonth: 12,
    cardExpiryYear: 2030,
    fields: [['Número', '5555 5555 5555 4444'], ['Titular', 'CLIENTE TOOL RENTAL'], ['Vencimiento', '12/30'], ['CVV', '123']],
  },
  {
    id: 'pse',
    label: 'PSE',
    subtitle: 'Débito bancario',
    icon: 'bank',
    paymentMethod: 'PSE',
    fields: [['Banco', 'Banco de pruebas PSE'], ['Tipo de cuenta', 'Ahorros'], ['Número de cuenta', '0000 0000 0010'], ['Titular', 'CLIENTE TOOL RENTAL']],
  },
  {
    id: 'mercadopago',
    label: 'Mercado Pago',
    subtitle: 'Billetera digital',
    icon: 'wallet',
    paymentMethod: 'MERCADO_PAGO',
    fields: [['Cuenta', 'comprador@toolrental.test'], ['Titular', 'CLIENTE TOOL RENTAL'], ['Documento', '1000000010'], ['Estado', 'Habilitada para pruebas']],
  },
  {
    id: 'paypal',
    label: 'PayPal',
    subtitle: 'Cuenta digital',
    icon: 'paypal',
    paymentMethod: 'PAYPAL',
    fields: [['Cuenta', 'buyer@toolrental.test'], ['Titular', 'CLIENTE TOOL RENTAL'], ['Identificador', 'PP-TEST-0010'], ['Estado', 'Habilitada para pruebas']],
  },
]

export function PaymentPage({reservation,session}) {
  const { hostedCheckout } = useFeatures()
  const requestedId = new URLSearchParams(window.location.search).get('reservationId') || reservation?.reservationId
  const cachedReservation=reservation?.persistedReservation&&String(entityId(reservation.persistedReservation.id))===String(requestedId)?reservation.persistedReservation:null
  const [storedReservation,setStoredReservation] = useState(()=>cachedReservation)
  const [loadState,setLoadState] = useState(()=>cachedReservation?'ready':session&&requestedId?'loading':'idle')
  const [method,setMethod] = useState('visa')
  const [busy,setBusy] = useState(false)
  const [error,setError] = useState('')
  const checkoutKey = useRef(null)

  useEffect(()=>{
    if(!session||!requestedId)return undefined
    if(storedReservation)return undefined
    let active=true
    setLoadState('loading')
    setError('')
    api.getReservation(requestedId).then(item=>{
      if(!active)return
      setStoredReservation(item)
      setLoadState(item?'ready':'missing')
    }).catch(reason=>{
      if(active){setError(reason.message);setLoadState('error')}
    })
    return()=>{active=false}
  },[session,requestedId,storedReservation])

  if(!session)return <EmptyState title="Ingresa para continuar el pago" text="La reserva y el importe se consultan desde tu cuenta." action={()=>navigate(`/login?returnTo=${encodeURIComponent(window.location.pathname+window.location.search)}`)} actionText="Ingresar"/>
  if(!requestedId)return <EmptyState title="Selecciona una reserva" text="Abre una reserva pendiente para elegir el medio de pago." action={()=>navigate('/cuenta')} actionText="Abrir mis reservas"/>
  if(loadState==='loading'||loadState==='idle')return <main className="payment-reference-page"><div className="payment-page-state page-shell" role="status"><Icon name="card" size={34}/><h1>Preparando el pago</h1><p>Consultando el importe registrado para tu reserva.</p></div></main>
  if(loadState==='error')return <EmptyState title="No pudimos preparar el pago" text={error||'Intenta de nuevo desde tus reservas.'} action={()=>navigate('/cuenta')} actionText="Volver a mis reservas"/>
  if(loadState==='missing')return <EmptyState title="No encontramos esa reserva" text="Comprueba que la reserva pertenezca a tu cuenta." action={()=>navigate('/cuenta')} actionText="Volver a mis reservas"/>

  const current=storedReservation||reservation
  const reservationStatus=current?.status||current?.reservationStatus
  if(reservationStatus!=='PENDING_PAYMENT')return <EmptyState title="Esta reserva no está pendiente de pago" text="Consulta su estado actualizado desde tu cuenta." action={()=>navigate('/cuenta')} actionText="Abrir mis reservas"/>

  const startDate=current.period?.startDate||current.startDate
  const returnDate=current.effectiveReturnDate||current.period?.returnDate||current.returnDate
  const rentalSubtotal=Number(current.rentalSubtotal?.amount??current.quote?.rentalSubtotal?.amount??0)
  const deliveryFee=Number(current.deliveryFee?.amount??current.quote?.deliveryFee?.amount??0)
  const total=Number(current.total?.amount??current.serverTotal?.amount??current.quote?.total?.amount??0)
  const reservationCode=String(entityId(current.id??current.reservationId)).slice(0,8).toUpperCase()
  const fulfillment=current.fulfillmentMethod==='SUPPLIER_DELIVERY'?'Entrega del proveedor':'Recogida con el proveedor'
  const selectedMethod=fixedPaymentMethods.find(option=>option.id===method)||fixedPaymentMethods[0]

  const selectMethod=id=>{
    setMethod(id)
    setError('')
    checkoutKey.current=null
  }

  const startCheckout=async event=>{
    event.preventDefault()
    if(!hostedCheckout||busy)return
    setBusy(true)
    setError('')
    checkoutKey.current??=crypto.randomUUID()
    try{
      const result=await api.createCheckout(requestedId,checkoutKey.current,{
        paymentMethod:selectedMethod.paymentMethod,
        cardNetwork:selectedMethod.cardNetwork??null,
        cardLastFour:selectedMethod.cardLastFour??null,
        cardExpiryMonth:selectedMethod.cardExpiryMonth??null,
        cardExpiryYear:selectedMethod.cardExpiryYear??null,
      })
      if(result.processor==='FICTIONAL_GATEWAY'&&result.paymentStatus==='READY'){
        await api.decideFictionalPayment(result.paymentId,'APPROVED')
        navigate(`/payment-return?paymentId=${encodeURIComponent(result.paymentId)}`)
        return
      }
      const target=result.checkoutUrl&&result.paymentStatus==='READY'?isAllowedMercadoPagoUrl(result.checkoutUrl):null
      if(!target)throw new Error('La pasarela no devolvió una dirección de pago válida.')
      window.location.assign(target.href)
    }catch(reason){setError(reason.message)}
    finally{setBusy(false)}
  }

  return <main className="payment-reference-page">
    <section className="payment-reference-hero"><div className="payment-reference-grid"/><div className="page-shell"><div><p>Pago seguro</p><h1>Elige cómo pagar</h1><span>Revisa los datos fijos del medio antes de confirmar.</span></div><img src="/assets/hero-rotary-hammer-1200.png" alt=""/></div></section>
    <div className="payment-progress page-shell" aria-label="Progreso del pago"><span className="done"><b><Icon name="check" size={15}/></b><strong>Reserva<small>Creada</small></strong></span><i/><span className="active"><b>2</b><strong>Medio de pago<small>Elige una opción</small></strong></span><i/><span><b>3</b><strong>Resultado<small>Validado por el servidor</small></strong></span></div>
    <div className="payment-reference-layout page-shell">
      <form className="payment-reference-form" onSubmit={startCheckout} aria-busy={busy}>
        <div className="payment-section-heading"><div><p className="kicker dark"><span/> Pago de la reserva</p><h2>Selecciona un medio</h2></div><span className="payment-environment">Ambiente de prueba</span></div>
        <fieldset className="payment-reference-methods"><legend className="sr-only">Medios de pago</legend>
          {fixedPaymentMethods.map(option=><button key={option.id} type="button" className={`${method===option.id?'active ':''}${option.id==='paypal'?'paypal':''}`} aria-pressed={method===option.id} onClick={()=>selectMethod(option.id)}><span className={`payment-method-icon ${option.id==='paypal'?'paypal':''}`}><Icon name={option.icon} size={22}/></span><span><strong>{option.label}</strong><small>{option.subtitle}</small></span><i/></button>)}
        </fieldset>
        <section className="fixed-payment-details" aria-live="polite"><div className="fixed-payment-heading"><span className="payment-method-icon"><Icon name={selectedMethod.icon} size={22}/></span><div><strong>Datos fijos de {selectedMethod.label}</strong><p>Estos valores pertenecen al ambiente de prueba y no se pueden modificar.</p></div></div><div className="fixed-payment-fields">{selectedMethod.fields.map(([label,value])=><label key={label}><span>{label}</span><input value={value} readOnly aria-readonly="true"/></label>)}</div></section>
        <div className="payment-data-boundary"><Icon name="shield" size={25}/><div><strong>Confirmación protegida por el servidor</strong><p>El medio elegido, el importe y el resultado quedan asociados a la reserva persistida.</p></div></div>
        <div className="payment-integrity-row"><span><Icon name="shield" size={16}/>Datos fijos</span><span><Icon name="check" size={16}/>Importe del servidor</span><span><Icon name="card" size={16}/>Registro persistente</span></div>
        {error?<div className="form-error" role="alert">{error}</div>:null}
        <button className="payment-confirm" type="submit" disabled={busy||!hostedCheckout}>{busy?'Procesando pago…':`Pagar con ${selectedMethod.label}`} <Icon name="arrow" size={18}/></button>
        {!hostedCheckout?<p className="payment-unavailable" role="status">El procesamiento de pagos no está disponible en este entorno.</p>:null}
        <button className="payment-back" type="button" onClick={()=>navigate('/cuenta')}>Volver a mis reservas</button>
        <p className="payment-terms"><Icon name="shield" size={13}/>Antes de pagar puedes consultar los <button type="button" onClick={()=>navigate('/terms-and-conditions')}>Términos y Condiciones</button>.</p>
      </form>
      <aside className="payment-reference-summary">
        <div className="payment-summary-header"><span><Icon name="calendar" size={20}/></span><div><small>Reserva</small><h2>TR-{reservationCode}</h2></div></div>
        <dl><div><dt><Icon name="calendar" size={14}/>Fechas</dt><dd>{startDate} → {returnDate}</dd></div><div><dt><Icon name="pin" size={14}/>Entrega</dt><dd>{fulfillment}</dd></div><div><dt><Icon name="card" size={14}/>Moneda</dt><dd>COP</dd></div></dl>
        <div className="payment-price-breakdown"><h3>Resumen del cobro</h3><p><span>Alquiler</span><strong>{money.format(rentalSubtotal)}</strong></p><p><span>Entrega</span><strong>{money.format(deliveryFee)}</strong></p></div>
        <div className="payment-reference-total"><span>Total</span><strong>{money.format(total)}</strong></div>
        <p className="payment-summary-note">El resultado se confirma desde el servidor y queda disponible en Mi cuenta.</p>
      </aside>
    </div>
  </main>
}
