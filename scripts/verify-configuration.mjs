import {execFileSync,spawnSync} from 'node:child_process'
import {readFileSync,writeFileSync} from 'node:fs'
import {resolve} from 'node:path'
import {randomBytes} from 'node:crypto'
let env=readFileSync('.env.showcase.example','utf8')
const values={MERCADOPAGO_ACCESS_TOKEN:"configuration-validation-only",MERCADOPAGO_COLLECTOR_ID:"123456",MERCADOPAGO_WEBHOOK_SECRET:randomBytes(48).toString("hex"),SANDBOX_WEBHOOK_SECRET:randomBytes(48).toString('hex'),PUBLIC_HOST:'tool-rental-validation.invalid',ACME_EMAIL:'evaluator@example.test',POSTGRES_PASSWORD:randomBytes(24).toString('hex'),JWT_SECRET:randomBytes(48).toString('hex'),BACKEND_IMAGE:'tool-rental-mvp-backend:working',FRONTEND_IMAGE:'tool-rental-mvp-frontend:working',SMTP_HOST:'mailpit',SMTP_USERNAME:'private-config-validation',SMTP_PASSWORD:randomBytes(24).toString('hex'),PASSWORD_RECOVERY_SENDER:'evaluator@example.test'}
for(const [key,value] of Object.entries(values))env=env.replace(new RegExp('^'+key+'=.*$','m'),key+'='+value)
writeFileSync('.env.showcase.validation',env,{mode:0o600})
execFileSync('docker',['compose','--env-file','.env.showcase.validation','-f','compose.showcase.yml','config','--quiet'],{stdio:'pipe'})
execFileSync('docker',['run','--rm','--env-file','.env.showcase.validation','-v',resolve('ops/Caddyfile')+':/etc/caddy/Caddyfile:ro','caddy:2.11.2-alpine','caddy','validate','--config','/etc/caddy/Caddyfile','--adapter','caddyfile'],{stdio:'pipe'})
execFileSync('docker',['run','--rm','-v',resolve('scripts')+':/scripts:ro','--entrypoint','/bin/sh','caddy:2.11.2-alpine','-c','for file in /scripts/*.sh; do sh -n "$file" || exit 1; done'],{stdio:'pipe'})
const release=spawnSync(process.execPath,['scripts/check-release.mjs'],{encoding:'utf8'})
if(release.status!==1)throw Error('The incomplete MVP must block public release')
writeFileSync('docs/evidence/mvp/configuration.json',JSON.stringify({testedAt:new Date().toISOString(),publicComposeValid:true,caddyConfigurationValid:true,operationalShellSyntaxValid:true,incompleteMvpReleaseRejected:true,publicServicesStarted:false},null,2)+'\n')
console.log('Public Compose/Caddy and shell syntax validated; incomplete release rejected; no public services started.')
