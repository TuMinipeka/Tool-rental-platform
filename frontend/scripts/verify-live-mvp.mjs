import {execFileSync} from 'node:child_process'
import {sourceFingerprint} from '../../scripts/source-fingerprint.mjs'
import { fileURLToPath } from 'node:url'
import {chromium,expect} from '@playwright/test'
import {mkdirSync,writeFileSync} from 'node:fs'
const base=process.env.MVP_BASE_URL || 'http://localhost:15173'
const evidenceRoot=fileURLToPath(new URL('../../docs/evidence/mvp/',import.meta.url))
if(!['http://localhost:15173','http://127.0.0.1:15173'].includes(base))throw Error('Write tests require the isolated local MVP environment')
const browser=await chromium.launch({channel:'chrome'})
let backendStopped=false
const project=['compose','--env-file','.env.validation','-f','compose.validation.yml']
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
const start=new Date(Date.now()+21*86400000).toISOString().slice(0,10)
const reservation=await api(customer.context,'/reservations',customer.token,{startDate:start,returnDate:start,equipmentUnitIds:[unitId],scheduledFulfillmentAt:start+'T14:00:00Z',fulfillmentMethod:'CUSTOMER_PICKUP'})
const id=reservation.id?.value??reservation.id
check('Inclusive single day total is 25000 COP',Number(reservation.total.amount)===25000)
await customer.page.goto(base+'/cuenta')
await expect(customer.page.locator('.reservation-row').filter({hasText:id.slice(0,8).toUpperCase()})).toBeVisible()
await supplier.page.reload()
const row=supplier.page.getByRole('region',{name:'Gestión de reservas'}).locator('article').filter({hasText:id.slice(0,8).toUpperCase()})
await row.getByRole('button',{name:'Confirmar solicitud'}).click()
await expect(row.locator('.status-pill')).toHaveText('CONFIRMED')
check('Supplier confirms through the actual browser UI',true)
await customer.page.reload()
const own=customer.page.locator('.reservation-row').filter({hasText:id.slice(0,8).toUpperCase()})
await expect(own).toContainText('CONFIRMED')
await own.getByRole('button',{name:'Solicitar cancelación'}).click()
await expect(own).toContainText('CANCELLED')
check('Customer cancels through the actual browser UI',true)
for(const kind of ['Alquileres','Pagos','Comprobantes']){await customer.page.getByRole('button',{name:new RegExp('^'+kind)}).click();await expect(customer.page.getByRole('region',{name:kind==='Pagos'?'Pagos registrados':kind==='Comprobantes'?'Comprobantes académicos':'Alquileres',exact:true})).toBeVisible()}
check('History panels consume real endpoints without fabricated reports',true)
const stored=await customer.page.evaluate(()=>JSON.stringify({...localStorage,...sessionStorage}))
check('Access token is absent from web storage',!stored.includes(customer.token))
const csrf=await api(customer.context,'/auth/csrf')
const renewed=await api(customer.context,'/auth/refresh',null,{}, {[csrf.headerName]:csrf.token})
check('Refresh cookie renews the actual session',Boolean(renewed.accessToken))
await api(customer.context,'/auth/logout',renewed.accessToken,{}, {[csrf.headerName]:csrf.token})
check('Logout immediately invalidates the access session',(await customer.context.request.get(base+'/api/v1/reservations',{headers:{Authorization:'Bearer '+renewed.accessToken}})).status()===401)
const admin=await login('admin');await admin.page.goto(base+'/panel/admin');await expect(admin.page.getByRole('heading',{name:'Usuarios',exact:true})).toBeVisible();check('Admin panel lists actual users',true)
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
}catch(failure){evidence.completed=false;evidence.failure=failure.message;throw failure}finally{
if(backendStopped){
 execFileSync('docker',[...project,'start','backend'],{stdio:'pipe'})
 let ready=false
 for(let attempt=0;attempt<45;attempt++){try{const response=await fetch(base+'/status/database');if(response.ok){ready=true;break}}catch{/* Readiness can fail while the isolated service starts. */}await new Promise(resolve=>setTimeout(resolve,1000))}
 if(!ready){evidence.completed=false;evidence.failure='Backend restart readiness failed'}
}
await browser.close();writeFileSync(evidenceRoot+'live-integration.json',JSON.stringify(evidence,null,2)+'\n')}
console.log('Actual integrated checks passed: '+evidence.checks.length)
