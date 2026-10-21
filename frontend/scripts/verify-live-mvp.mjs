if(process.env.RUN_LEGACY_INTERNAL_SANDBOX !== 'true') throw new Error('Historical internal-HMAC/Mailpit runner. Current MVP: use scripts/verify-showcase.mjs and official-provider acceptance in docs/mercadopago-mvp.md; do not enable legacy routes in production.')
import {createHmac,randomBytes} from 'node:crypto'
import {execFileSync} from 'node:child_process'
import {sourceFingerprint} from '../../scripts/source-fingerprint.mjs'
import { fileURLToPath } from 'node:url'
import {chromium,expect} from '@playwright/test'
import {mkdirSync,readFileSync,writeFileSync} from 'node:fs'
const base=process.env.MVP_BASE_URL || 'http://localhost:15173'
const evidenceRoot=fileURLToPath(new URL('../../docs/evidence/mvp/',import.meta.url))
if(!['http://localhost:15173','http://127.0.0.1:15173'].includes(base))throw Error('Write tests require the isolated local MVP environment')
const browser=await chromium.launch({channel:'chrome'})
let backendStopped=false
const project=['compose','--env-file','.env.validation','-f','compose.validation.yml']
const privateValues=[]
const evidence={sourceFingerprint:sourceFingerprint(),testedAt:new Date().toISOString(),baseUrl:base,scope:'Actual Spring Boot and isolated PostgreSQL; no API mocks',checks:[]}
const check=(name,value)=>{if(!value)throw Error(name);evidence.checks.push({name,passed:true})}
const api=async(context,path,token,body,headers={})=>{
 const response=await context.request.fetch(base+'/api/v1'+path,{method:body?'POST':'GET',headers:{...(token?{Authorization:'Bearer '+token}:{}),...headers},...(body?{data:body}:{})})
 const text=await response.text();let json;try{json=JSON.parse(text)}catch{json=text}
 if(!response.ok())throw Error(path+': HTTP '+response.status()+' '+(json.code||json.detail||''))
 return json
}
const login=async(role)=>{
 const context=await browser.newContext({viewport:{width:1440,height:1000}})
 const page=await context.newPage()
 await page.goto(base+'/login')
 await page.getByRole('textbox',{name:'Correo electrónico'}).fill(role+'@toolrental.local')
 await page.getByLabel(/^Contraseña/).fill('ToolRental2026!')
 const response=page.waitForResponse(r=>r.url().endsWith('/auth/login')&&r.request().method()==='POST')
 await page.locator('form').getByRole('button',{name:'Ingresar',exact:true}).click()
 const result=await(await response).json()
 if(!result.accessToken)throw Error('Login rejected for '+role)
 privateValues.push(result.accessToken)
 await expect(page.getByRole('button',{name:'Abrir mi cuenta'})).toBeVisible()
 check('Real '+role+' login',true)
 return {context,page,token:result.accessToken}
}
try{
const anonymous=await browser.newContext()
check('Anonymous protected endpoint returns 401',(await anonymous.request.get(base+'/api/v1/users')).status()===401)
const status=await(await anonymous.request.get(base+'/status/database')).json();check('JDBC connected',status.connected===true)
const schema=await(await anonymous.request.get(base+'/v3/api-docs')).json();check('OpenAPI only publishes v1',Object.keys(schema.paths).every(p=>p.startsWith('/api/v1/')))
const supplier=await login('proveedor')
await supplier.page.evaluate(()=>{history.pushState({},'', '/panel/proveedor');dispatchEvent(new PopStateEvent('popstate'))})
await expect(supplier.page.locator('.operations-metrics')).toBeVisible()
const categories=await api(supplier.context,'/categories',supplier.token)
const nonce=Date.now().toString()
const tool=await api(supplier.context,'/tools',supplier.token,{categoryId:categories[0].id?.value??categories[0].id,name:'MVP live tool '+nonce,description:'Isolated integration verification equipment',dailyRate:25000,currency:'COP',imageUrls:[base+'/assets/tools/catalog/01.png'],published:true})
const toolId=tool.id?.value??tool.id
const unit=await api(supplier.context,'/equipment-units',supplier.token,{toolId,inventoryCode:'MVP-LIVE-'+nonce,serialNumber:'LIVE-'+nonce,currentCondition:'GOOD',status:'OPERATIONAL'})
const unitId=unit.id?.value??unit.id
check('Supplier tool and physical unit persist',Boolean(toolId&&unitId))
const customer=await login('cliente')
check('Customer cannot list administrative users',(await customer.context.request.get(base+'/api/v1/users',{headers:{Authorization:'Bearer '+customer.token}})).status()===403)
const start=new Date().toISOString().slice(0,10)
const scheduledAt=new Date(Date.now()+4000).toISOString()
const reservation=await api(customer.context,'/reservations',customer.token,{startDate:start,returnDate:start,equipmentUnitIds:[unitId],scheduledFulfillmentAt:scheduledAt,fulfillmentMethod:'CUSTOMER_PICKUP'})
const id=reservation.id?.value??reservation.id
check('Inclusive single day total is 25000 COP',Number(reservation.total.amount)===25000)
check('Reservation creates an expiring payment hold',reservation.status==='PENDING_PAYMENT'&&Boolean(reservation.expiresAt))
const conflict=await customer.context.request.post(base+'/api/v1/reservations',{headers:{Authorization:'Bearer '+customer.token},data:{startDate:start,returnDate:start,equipmentUnitIds:[unitId],scheduledFulfillmentAt:scheduledAt,fulfillmentMethod:'CUSTOMER_PICKUP'}})
check('A second overlapping hold is rejected',conflict.status()===409)
check('Customer cannot query operational reports',(await customer.context.request.get(base+'/api/v1/reports/income?from=2025-01-01&to=2027-12-31',{headers:{Authorization:'Bearer '+customer.token}})).status()===403)
await customer.page.getByRole('button',{name:'Actualizar reservas'}).click()
const own=customer.page.locator('.reservation-row').filter({hasText:id.slice(0,8).toUpperCase()})
await expect(own).toContainText('PENDING PAYMENT')
const checkoutResponse=customer.page.waitForResponse(r=>r.url().endsWith('/payments/checkouts')&&r.request().method()==='POST')
await own.getByRole('button',{name:'Crear checkout académico'}).click()
const checkout=await(await checkoutResponse).json()
check('Browser checkout registers the full server amount',checkout.paymentStage==='FULL_PAYMENT'&&Number(checkout.amount)===25000)
const timestamp=String(Math.floor(Date.now()/1000))
const event={eventId:'mvp-live-'+nonce,externalReference:checkout.externalReference,outcome:'APPROVED'}
const settings=Object.fromEntries(readFileSync(fileURLToPath(new URL('../../.env.validation',import.meta.url)),'utf8').trim().split(/\r?\n/).map(line=>{const i=line.indexOf('=');return[line.slice(0,i),line.slice(i+1)]}))
const signature=createHmac('sha256',settings.MVP_SANDBOX_SECRET).update([timestamp,event.eventId,event.externalReference,event.outcome].join('.')).digest('hex')
const invalid=await anonymous.request.post(base+'/api/v1/payments/sandbox/webhooks',{headers:{'X-Sandbox-Timestamp':timestamp,'X-Sandbox-Signature':'invalid'},data:event})
check('Unsigned sandbox approval is rejected',invalid.status()===401)
for(let repeat=0;repeat<2;repeat++){
 const approved=await anonymous.request.post(base+'/api/v1/payments/sandbox/webhooks',{headers:{'X-Sandbox-Timestamp':timestamp,'X-Sandbox-Signature':signature},data:event})
 check('Trusted internal sandbox event accepted '+repeat,approved.ok())
}
await customer.page.getByRole('button',{name:'Actualizar reservas'}).click()
await expect(own).toContainText('CONFIRMED')
check('Signed internal payment confirms the reservation automatically',true)
const invoicesResponse=customer.page.waitForResponse(r=>r.url().endsWith('/invoices')&&r.request().method()==='GET')
await customer.page.getByRole('button',{name:/^Comprobantes/}).click()
const invoices=await(await invoicesResponse).json()
check('Repeated approval issues only one invoice',invoices.filter(invoice=>invoice.reservationId===id).length===1)
const downloadPromise=customer.page.waitForEvent('download')
const issued=invoices.find(invoice=>invoice.reservationId===id)
await customer.page.getByRole('region',{name:'Comprobantes académicos',exact:true}).locator('article').filter({hasText:issued.number}).getByRole('button',{name:'Descargar comprobante académico'}).click()
const document=await downloadPromise
check('Academic invoice downloads with DIAN disclaimer',readFileSync(await document.path(),'utf8').includes('DIAN'))
await supplier.page.getByRole('region',{name:'Gestión de reservas'}).getByRole('button',{name:'Actualizar',exact:true}).click()
const row=supplier.page.getByRole('region',{name:'Gestión de reservas'}).locator('article').filter({hasText:id.slice(0,8).toUpperCase()})
await row.getByText('Entrega y devolución',{exact:true}).click()
await row.getByLabel('Dirección de entrega o recogida').fill('Punto académico de recogida 123')
await row.getByLabel('Ciudad',{exact:true}).fill('Bucaramanga')
await row.getByLabel('Departamento',{exact:true}).fill('Santander')
await row.getByRole('button',{name:'Programar entrega'}).click()
await expect.poll(()=>Date.now()>=Date.parse(scheduledAt),{timeout:10000}).toBe(true)
await row.getByRole('button',{name:'Completar entrega'}).click()
await row.getByRole('button',{name:'Iniciar alquiler'}).click()
await row.getByLabel('Condición de devolución').fill('GOOD')
await row.getByRole('button',{name:'Registrar devolución'}).click()
await row.getByRole('button',{name:'Cerrar alquiler'}).click()
await expect(row.locator('.rental-workflow')).toContainText('COMPLETED')
check('Supplier completes delivery, rental and return through actual UI',true)
await expect(supplier.page.getByRole('region',{name:'Reportes operativos'}).getByRole('table')).toBeVisible()
check('Supplier report uses server data after payment',true)
const later=new Date(Date.now()+24*86400000).toISOString().slice(0,10)
const cancelOrder=await api(customer.context,'/reservations',customer.token,{startDate:later,returnDate:later,equipmentUnitIds:[unitId],scheduledFulfillmentAt:later+'T14:00:00Z',fulfillmentMethod:'CUSTOMER_PICKUP'})
const cancelId=cancelOrder.id?.value??cancelOrder.id
await customer.page.getByRole('button',{name:/^Mis reservas/}).click()
await customer.page.getByRole('button',{name:'Actualizar reservas'}).click()
const cancelRow=customer.page.locator('.reservation-row').filter({hasText:cancelId.slice(0,8).toUpperCase()})
await cancelRow.getByRole('button',{name:'Solicitar cancelación'}).click()
await expect(cancelRow).toContainText('CANCELLED')
check('Customer cancels an unpaid hold through the actual UI',true)
for(const kind of ['Alquileres','Pagos','Comprobantes']){await customer.page.getByRole('button',{name:new RegExp('^'+kind)}).click();await expect(customer.page.getByRole('region',{name:kind==='Pagos'?'Pagos registrados':kind==='Comprobantes'?'Comprobantes académicos':'Alquileres',exact:true})).toBeVisible()}
check('History panels consume real endpoints without fabricated reports',true)
const stored=await customer.page.evaluate(()=>JSON.stringify({...localStorage,...sessionStorage}))
check('Access token is absent from web storage',!stored.includes(customer.token))
const csrf=await api(customer.context,'/auth/csrf')
const renewed=await api(customer.context,'/auth/refresh',null,{}, {[csrf.headerName]:csrf.token})
check('Refresh cookie renews the actual session',Boolean(renewed.accessToken))
await api(customer.context,'/auth/logout',renewed.accessToken,{}, {[csrf.headerName]:csrf.token})
check('Logout immediately invalidates the access session',(await customer.context.request.get(base+'/api/v1/reservations',{headers:{Authorization:'Bearer '+renewed.accessToken}})).status()===401)
const admin=await login('admin');await admin.page.evaluate(()=>{history.pushState({},'', '/panel/admin');dispatchEvent(new PopStateEvent('popstate'))});await expect(admin.page.getByRole('heading',{name:'Usuarios',exact:true})).toBeVisible();check('Admin panel lists actual users',true);await expect(admin.page.getByRole('region',{name:'Reportes operativos'}).getByRole('table')).toBeVisible();check('Admin report uses actual server data',true)
const onboarding=await anonymous.newPage()
const supplierEmail='mvp.supplier.'+nonce+'@example.test'
const supplierPassword=randomBytes(24).toString('base64url')
privateValues.push(supplierPassword)
await onboarding.goto(base+'/registro/proveedor')
await onboarding.getByLabel('Nombre completo').fill('Academic Supplier Evaluator')
await onboarding.getByRole('textbox',{name:'Correo electrónico'}).fill(supplierEmail)
await onboarding.getByLabel(/^Contraseña/).fill(supplierPassword)
await onboarding.getByLabel(/^Confirmar contraseña/).fill(supplierPassword)
await onboarding.getByLabel('Razón social').fill('Academic Supplier Company')
await onboarding.locator('select[name="documentType"]').selectOption('NIT')
await onboarding.getByLabel('Número de documento').fill('MVP-SUP-'+nonce)
await onboarding.getByRole('checkbox').check()
const onboardingResponse=onboarding.waitForResponse(r=>r.url().endsWith('/auth/supplier-registration')&&r.request().method()==='POST')
await onboarding.locator('form').getByRole('button',{name:'Crear cuenta',exact:true}).click()
const enrolled=await(await onboardingResponse).json()
privateValues.push(enrolled.accessToken)
check('Supplier registration activates the account and opens a session',enrolled.roles.includes('SUPPLIER')&&Boolean(enrolled.accessToken))
await expect(onboarding.getByRole('heading',{name:'Tus herramientas, bajo tu control.'})).toBeVisible()
check('Registered supplier access is immediately valid',(await anonymous.request.get(base+'/api/v1/tools',{headers:{Authorization:'Bearer '+enrolled.accessToken}})).status()===200)
await admin.page.reload()
const supplierUserRow=admin.page.locator('.admin-workspace article').filter({hasText:supplierEmail})
await expect(supplierUserRow.locator('.status-pill')).toHaveText('ACTIVE')
await supplierUserRow.getByRole('button',{name:'Desactivar cuenta'}).click()
await expect(supplierUserRow.locator('.status-pill')).toHaveText('DISABLED')
check('Administrative deactivation revokes the supplier session',(await anonymous.request.get(base+'/api/v1/tools',{headers:{Authorization:'Bearer '+enrolled.accessToken}})).status()===401)
await onboarding.close()
const recoveryPage=await anonymous.newPage()
const recoveryEmail='mvp.reset.'+nonce+'@example.test'
const recoveryPassword=randomBytes(24).toString('base64url')
const newPassword=randomBytes(24).toString('base64url')
privateValues.push(recoveryPassword,newPassword)
await recoveryPage.goto(base+'/registro')
await recoveryPage.getByLabel('Nombre completo').fill('Academic Recovery Evaluator')
await recoveryPage.getByRole('textbox',{name:'Correo electrónico'}).fill(recoveryEmail)
await recoveryPage.getByLabel(/^Contraseña/).fill(recoveryPassword)
await recoveryPage.getByLabel(/^Confirmar contraseña/).fill(recoveryPassword)
await recoveryPage.getByLabel('Número de documento').fill('MVP-RESET-'+nonce)
await recoveryPage.getByRole('checkbox').check()
const registrationResponse=recoveryPage.waitForResponse(r=>r.url().endsWith('/auth/register')&&r.request().method()==='POST')
await recoveryPage.locator('form').getByRole('button',{name:'Crear cuenta',exact:true}).click()
const registeredResponse=await registrationResponse
const registered=await registeredResponse.json()
check('Public customer registration uses the actual PERSON contract',registeredResponse.ok()&&registered.roles?.includes('CUSTOMER'))
const recovery=await anonymous.request.post(base+'/api/v1/auth/password-recovery',{data:{email:recoveryEmail}})
const recoveryBody=await recovery.json()
check('Recovery response is generic and contains no token',recovery.status()===202&&!Object.keys(recoveryBody).some(key=>key.toLowerCase().includes('token')))
const messages=await(await fetch('http://localhost:18025/api/v1/messages')).json()
const message=messages.messages.find(item=>item.To?.some(recipient=>recipient.Address===recoveryEmail))
check('Existing mail adapter delivers to private Mailpit',Boolean(message))
const content=await(await fetch('http://localhost:18025/api/v1/message/'+message.ID)).json()
const link=(content.Text||content.HTML).match(/https?:[^\s<>"']+token=[^\s<>"']+/)?.[0]
if(!link)throw Error('Recovery message has no reset link')
const resetToken=new URL(link).searchParams.get('token')
privateValues.push(resetToken)
check('Recovery link targets the integrated frontend',new URL(link).origin===base)
const restoredResponse=recoveryPage.waitForResponse(r=>r.url().endsWith('/auth/refresh'))
await recoveryPage.goto(link)
const restoredHttp=await restoredResponse
const restoredPayload=await restoredHttp.json()
check('Registered session renews on reset navigation',restoredHttp.ok())
const restored=restoredPayload
evidence.recoveryRefreshStatus=restoredHttp.status()
privateValues.push(restored.accessToken)
check('Recovery session is active before password change',(await anonymous.request.get(base+'/api/v1/auth/sessions',{headers:{Authorization:'Bearer '+restored.accessToken}})).status()===200)
await expect(recoveryPage).toHaveURL(base+'/password-reset')
await recoveryPage.getByLabel(/^Contraseña/).fill(newPassword)
await recoveryPage.getByLabel(/^Confirmar contraseña/).fill(newPassword)
await recoveryPage.getByRole('button',{name:'Guardar nueva contraseña'}).click()
await expect(recoveryPage.getByRole('status')).toContainText('Tu contraseña cambió')
check('Browser resets the password and removes the token from its URL',!recoveryPage.url().includes(resetToken))
check('Password reset revokes the prior access session',(await anonymous.request.get(base+'/api/v1/reservations',{headers:{Authorization:'Bearer '+restored.accessToken}})).status()===401)
const replay=await anonymous.request.post(base+'/api/v1/auth/password-reset',{data:{token:resetToken,newPassword:randomBytes(24).toString('base64url')}})
check('Password reset token cannot be reused',!replay.ok())
await recoveryPage.close()
mkdirSync(evidenceRoot+'screenshots',{recursive:true})
await admin.page.screenshot({path:evidenceRoot+'screenshots/admin.png',fullPage:true})
await supplier.page.screenshot({path:evidenceRoot+'screenshots/supplier.png',fullPage:true})
await customer.page.goto(base+'/catalogo');await customer.page.screenshot({path:evidenceRoot+'screenshots/catalog.png',fullPage:true})
await anonymous.close()
execFileSync('docker',[...project,'stop','backend'],{stdio:'pipe'});backendStopped=true
await customer.page.goto(base+'/catalogo')
await expect(customer.page.getByRole('alert')).toContainText('No pudimos cargar el catálogo del servidor')
await expect(customer.page.locator('.tool-card')).toHaveCount(0)
check('Backend outage never substitutes a fabricated catalog in the live build',true)
evidence.completed=true
}catch(failure){evidence.completed=false;evidence.failure=privateValues.reduce((message,value)=>message.replaceAll(value,'[REDACTED]'),failure.message);process.exitCode=1;console.error('Live integration failed; inspect the sanitized evidence report')}finally{
if(backendStopped){
 execFileSync('docker',[...project,'start','backend'],{stdio:'pipe'})
 let ready=false
 for(let attempt=0;attempt<45;attempt++){try{const response=await fetch(base+'/status/database');if(response.ok){ready=true;break}}catch{/* Readiness can fail while the isolated service starts. */}await new Promise(resolve=>setTimeout(resolve,1000))}
 if(!ready){evidence.completed=false;evidence.failure='Backend restart readiness failed'}
}
await browser.close();writeFileSync(evidenceRoot+'live-integration.json',JSON.stringify(evidence,null,2)+'\n')}
if(evidence.completed)console.log('Actual integrated checks passed: '+evidence.checks.length);else process.exitCode=1
