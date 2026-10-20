import {execFileSync} from 'node:child_process'
import {randomBytes} from 'node:crypto'
import {writeFileSync} from 'node:fs'
import {sourceFingerprint} from './source-fingerprint.mjs'
const compose=['compose','--env-file','.env.validation','-f','compose.validation.yml','-f','compose.secure-validation.yml','--profile','security']
const docker=(args,env)=>execFileSync('docker',args,{encoding:'utf8',env:env||process.env})
const sql=query=>docker([...compose,'exec','-T','postgres','psql','-X','-qAt','-v','ON_ERROR_STOP=1','-U','mvp_validation','-d','mvp_validation','-c',query]).trim()
if(sql("SELECT count(*) FROM pg_database WHERE datname='mvp_showcase'")==='0')sql('CREATE DATABASE mvp_showcase OWNER mvp_validation')
let started=false
const evidence={testedAt:new Date().toISOString(),sourceFingerprint:sourceFingerprint(),scope:'Private production+showcase profile; no public HTTPS or SMTP delivery validated',checks:[]}
const check=(name,result)=>{if(!result)throw Error(name);evidence.checks.push({name,passed:true})}
try{
docker([...compose,'up','-d','--no-build','secure-backend']);started=true
let ready=false
for(let i=0;i<60;i++){try{if((await fetch('http://localhost:18081/status/database')).ok){ready=true;break}}catch{/* Readiness can fail while the isolated service starts. */}await new Promise(r=>setTimeout(r,1000))}
check('Secure profile starts against an isolated showcase database',ready)
const rejected=await fetch('http://localhost:18081/api/v1/auth/login',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({email:'admin@toolrental.local',password:'ToolRental2026!'})})
check('Known demonstration administrator credentials are disabled',rejected.status===401)
for(const role of ['CUSTOMER','SUPPLIER','ADMIN']){
 const password=randomBytes(24).toString('base64url')
 const email='evaluator.'+role.toLowerCase()+'@example.test'
 docker([...compose,'exec','-T','-e','EVALUATOR_EMAIL','-e','EVALUATOR_PASSWORD','-e','EVALUATOR_ROLE','secure-backend','java','-cp','infrastructure-0.1.0-SNAPSHOT.jar:lib/*','com.toolrental.infrastructure.common.bootstrap.EvaluatorProvisioner'],{...process.env,EVALUATOR_EMAIL:email,EVALUATOR_PASSWORD:password,EVALUATOR_ROLE:role})
 const response=await fetch('http://localhost:18081/api/v1/auth/login',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({email,password})})
 check('Privately provisioned '+role+' can authenticate',response.ok)
 const authentication=await response.json()
 const cookie=response.headers.get('set-cookie')||''
 check(role+' refresh cookie is Secure, HttpOnly and SameSite=Lax',cookie.includes('Secure')&&cookie.includes('HttpOnly')&&cookie.includes('SameSite=Lax'))
 docker([...compose,'exec','-T','-e','EVALUATOR_EMAIL','-e','EVALUATOR_PASSWORD','-e','EVALUATOR_ROLE','secure-backend','java','-cp','infrastructure-0.1.0-SNAPSHOT.jar:lib/*','com.toolrental.infrastructure.common.bootstrap.EvaluatorProvisioner'],{...process.env,EVALUATOR_EMAIL:email,EVALUATOR_PASSWORD:randomBytes(24).toString('base64url'),EVALUATOR_ROLE:role})
 const revoked=await fetch('http://localhost:18081/api/v1/auth/sessions',{headers:{Authorization:'Bearer '+authentication.accessToken}})
 check(role+' offline credential rotation revokes prior access',revoked.status===401)
}
evidence.completed=true
} catch(error){evidence.completed=false;evidence.failure=error.message;throw error}
finally{if(started)docker([...compose,'stop','secure-backend']);writeFileSync('docs/evidence/mvp/secure-profile.json',JSON.stringify(evidence,null,2)+'\n')}
console.log('Private showcase profile checks passed: '+evidence.checks.length)
