import {execFileSync} from 'node:child_process'
import {readFileSync,writeFileSync} from 'node:fs'
import {sourceFingerprint} from './source-fingerprint.mjs'
const settings=Object.fromEntries(readFileSync('.env.validation','utf8').trim().split(/\r?\n/).map(line=>{const i=line.indexOf('=');return [line.slice(0,i),line.slice(i+1)]}))
const db=settings.MVP_DB_NAME||'mvp_validation'
if(!/^mvp_[a-z0-9_]+$/.test(db))throw Error('An isolated validation database is required')
const compose=['compose','--env-file','.env.validation','-f','compose.validation.yml']
const docker=args=>execFileSync('docker',args,{encoding:'utf8',maxBuffer:8*1024*1024})
const pg=(database,sql)=>docker([...compose,'exec','-T','postgres','psql','-X','-qAt','-v','ON_ERROR_STOP=1','-U','mvp_validation','-d',database,'-c',sql]).trim()
const names=JSON.parse(pg(db,"SELECT json_agg(tablename ORDER BY tablename) FROM pg_tables WHERE schemaname='public'"))
const counts=database=>Object.fromEntries(names.map(name=>[name,Number(pg(database,'SELECT count(*) FROM '+name))]))
const before=counts(db)
const restored='mvp_restore_'+Date.now()
docker([...compose,'exec','-T','postgres','pg_dump','-U','mvp_validation','-d',db,'-Fc','--no-owner','--no-acl','-f','/tmp/mvp-validation.dump'])
docker([...compose,'exec','-T','postgres','createdb','-U','mvp_validation',restored])
docker([...compose,'exec','-T','postgres','pg_restore','-U','mvp_validation','-d',restored,'--no-owner','--no-acl','--exit-on-error','/tmp/mvp-validation.dump'])
const afterRestore=counts(restored)
if(JSON.stringify(before)!==JSON.stringify(afterRestore))throw Error('Restored row counts differ')
// Exercise only the dedicated MVP project, never the working stack.
docker([...compose,'restart','postgres'])
for(let attempt=0;attempt<30;attempt++){try{docker([...compose,'exec','-T','postgres','pg_isready','-U','mvp_validation']);break}catch{await new Promise(resolve=>setTimeout(resolve,1000))}}
docker([...compose,'restart','backend'])
let connected=false
for(let attempt=0;attempt<45;attempt++){try{const response=await fetch('http://localhost:15173/status/database');const data=await response.json();if(data.connected){connected=true;break}}catch{/* Readiness can fail while the isolated service starts. */}await new Promise(resolve=>setTimeout(resolve,1000))}
if(!connected)throw Error('Backend did not reconnect after restart')
const afterRestart=counts(db)
if(JSON.stringify(before)!==JSON.stringify(afterRestart))throw Error('Persistent row counts differ')
const dump=docker([...compose,'exec','-T','postgres','sha256sum','/tmp/mvp-validation.dump']).split(' ')[0]
writeFileSync('docs/evidence/mvp/backup-restore.json',JSON.stringify({testedAt:new Date().toISOString(),sourceFingerprint:sourceFingerprint(),scope:'Private isolated MVP database; offsite transfer is not validated',dumpSha256:dump,restoredDatabase:restored,before,afterRestore,afterRestart,restorationPassed:true,persistencePassed:true,offsiteCopyPassed:false},null,2)+'\n')
console.log('Backup restored with matching counts in '+names.length+' tables; restart persisted data and JDBC reconnected.')
