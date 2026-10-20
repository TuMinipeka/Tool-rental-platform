import {createHmac,randomUUID} from 'node:crypto'
import {readFileSync} from 'node:fs'
const settings=Object.fromEntries(readFileSync('.env.validation','utf8').trim().split(/\r?\n/).map(line=>{const index=line.indexOf('=');return[line.slice(0,index),line.slice(index+1)]}))
const reference=process.env.SANDBOX_EXTERNAL_REFERENCE
const outcome=process.env.SANDBOX_OUTCOME||'APPROVED'
if(!/^sandbox_[a-f0-9-]{36}$/.test(reference||'')||!['APPROVED','DECLINED'].includes(outcome)||!settings.MVP_SANDBOX_SECRET)throw Error('Set a valid private sandbox reference, outcome and validation environment')
const event={eventId:randomUUID(),externalReference:reference,outcome}
const timestamp=String(Math.floor(Date.now()/1000))
const signature=createHmac('sha256',settings.MVP_SANDBOX_SECRET).update([timestamp,event.eventId,reference,outcome].join('.')).digest('hex')
const response=await fetch('http://localhost:15173/api/v1/payments/sandbox/webhooks',{method:'POST',headers:{'Content-Type':'application/json','X-Sandbox-Timestamp':timestamp,'X-Sandbox-Signature':signature},body:JSON.stringify(event)})
if(!response.ok)throw Error('Internal sandbox rejected the event: HTTP '+response.status)
console.log('Internal academic sandbox event accepted. No external charge was executed.')
