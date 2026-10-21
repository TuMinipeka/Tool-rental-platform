import {createHash} from 'node:crypto'
import {existsSync,readdirSync,readFileSync} from 'node:fs'
import {join,relative} from 'node:path'
import {fileURLToPath} from 'node:url'
export function sourceFingerprint(){
 const root=fileURLToPath(new URL('../',import.meta.url))
 const roots=['domain/src','application/src','infrastructure/src','frontend/src','frontend/public','frontend/scripts','frontend/tests','ops','scripts']
 const files=[]
 const walk=path=>{if(!existsSync(path))return;for(const item of readdirSync(path,{withFileTypes:true})){const full=join(path,item.name);if(item.isDirectory())walk(full);else files.push(full)}}
 for(const path of roots)walk(join(root,path))
 for(const path of ['pom.xml','domain/pom.xml','application/pom.xml','infrastructure/pom.xml','Dockerfile','.dockerignore','.gitignore','frontend/Dockerfile','frontend/nginx.conf','frontend/package.json','frontend/package-lock.json','frontend/eslint.config.js','frontend/playwright.config.js','compose.yml','compose.validation.yml','compose.secure-validation.yml','compose.showcase.yml'])files.push(join(root,path))
 const hash=createHash('sha256')
 for(const file of files.sort()){hash.update(relative(root,file).replaceAll('\\','/'));hash.update('\0');hash.update(readFileSync(file));hash.update('\0')}
 return hash.digest('hex')
}
