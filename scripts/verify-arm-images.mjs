import {execFileSync} from 'node:child_process'
import {mkdirSync,writeFileSync} from 'node:fs'
const images=['eclipse-temurin:21-jdk-jammy','eclipse-temurin:21-jre-jammy','node:24.22.0-alpine','nginx:1.30.6-alpine-slim','postgres:17.11-alpine','caddy:2.11.2-alpine']
const results=[]
for(const image of images){const manifest=JSON.parse(execFileSync('docker',['buildx','imagetools','inspect',image,'--format','{{json .Manifest}}'],{encoding:'utf8'}));const platforms=manifest.manifests.map(m=>m.platform).filter(p=>p.os==='linux');if(!platforms.some(p=>p.architecture==='arm64'))throw Error('ARM64 unavailable: '+image);results.push({image,digest:manifest.digest,platforms});console.log('ARM64 manifest verified: '+image)}
mkdirSync('docs/evidence/mvp',{recursive:true})
writeFileSync('docs/evidence/mvp/arm-manifests.json',JSON.stringify({verifiedAt:new Date().toISOString(),runtimeOnOracleVerified:false,images:results},null,2)+'\n')
