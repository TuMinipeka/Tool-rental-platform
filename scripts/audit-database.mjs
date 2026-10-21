import {sourceFingerprint} from './source-fingerprint.mjs'
import {execFileSync} from 'node:child_process'
import {writeFileSync,mkdirSync,readFileSync} from 'node:fs'
const settings=Object.fromEntries(readFileSync('.env.validation','utf8').trim().split(/\r?\n/).map(line=>{const at=line.indexOf('=');return [line.slice(0,at),line.slice(at+1)]}))
const database=settings.MVP_DB_NAME||'mvp_validation'
const compose=['compose','--env-file','.env.validation','-f','compose.validation.yml','exec','-T','postgres']
const query=sql=>JSON.parse(execFileSync('docker',[...compose,'psql','-X','-q','-A','-t','-v','ON_ERROR_STOP=1','-U','mvp_validation','-d',database,'-c',sql],{encoding:'utf8'}))
const tables=query(`SELECT json_agg(tablename ORDER BY tablename) FROM pg_tables WHERE schemaname='public' AND tablename<>'flyway_schema_history'`)
const exceptions={roles:'Controlled authorization vocabulary, not 20 invented roles',user_sessions:'Created by actual authentication; no seeded sessions',password_reset_tokens:'Created by actual recovery; no seeded secrets',cancellation_policies:'Versioned active policy configuration',delivery_rates:'Versioned active delivery configuration'}
const counts=tables.map(table=>({table,rows:query('SELECT count(*) FROM '+table),minimum:table in exceptions?null:20,exception:exceptions[table]||null}))
const columns=query(`SELECT json_agg(row_to_json(c) ORDER BY table_name,ordinal_position) FROM (SELECT table_name,column_name,data_type,is_nullable,ordinal_position FROM information_schema.columns WHERE table_schema='public' AND table_name<>'flyway_schema_history') c`)
const constraints=query(`SELECT json_agg(row_to_json(c) ORDER BY table_name,name) FROM (SELECT conrelid::regclass::text AS table_name,conname AS name,contype AS type,pg_get_constraintdef(oid) AS definition,convalidated AS validated FROM pg_constraint WHERE connamespace='public'::regnamespace) c`)
const indexes=query(`SELECT json_agg(row_to_json(i) ORDER BY tablename,indexname) FROM pg_indexes i WHERE schemaname='public'`)
const relationships=query(`SELECT json_agg(row_to_json(f) ORDER BY child,name) FROM (SELECT c.conname AS name,c.conrelid::regclass::text AS child,c.confrelid::regclass::text AS parent,EXISTS(SELECT 1 FROM pg_constraint u WHERE u.conrelid=c.conrelid AND u.contype IN ('p','u') AND u.conkey=c.conkey) AS child_unique,NOT EXISTS(SELECT 1 FROM unnest(c.conkey) k JOIN pg_attribute a ON a.attrelid=c.conrelid AND a.attnum=k WHERE NOT a.attnotnull) AS required FROM pg_constraint c WHERE c.contype='f' AND c.connamespace='public'::regnamespace) f`)
const migrations=query(`SELECT json_agg(row_to_json(f) ORDER BY installed_rank) FROM (SELECT installed_rank,version,description,script,checksum,success FROM flyway_schema_history) f`)
const report={generatedAt:new Date().toISOString(),sourceFingerprint:sourceFingerprint(),validatedWorkingTree:true,sourceCommit:execFileSync('git',['rev-parse','HEAD'],{encoding:'utf8'}).trim(),database:'isolated MVP validation',tables:counts,columns,constraints,indexes,relationships,migrations}
mkdirSync('docs/evidence/mvp',{recursive:true});mkdirSync('docs/diagrams',{recursive:true})
writeFileSync('docs/evidence/mvp/database-audit.json',JSON.stringify(report,null,2)+'\n')
let diagram='erDiagram\n'
for(const table of tables){diagram+='    '+table+' {\n';for(const c of columns.filter(c=>c.table_name===table)){const pk=constraints.find(k=>k.table_name===table&&k.type==='p'&&new RegExp('\\b'+c.column_name+'\\b').test(k.definition));const fk=constraints.find(k=>k.table_name===table&&k.type==='f'&&k.definition.split('REFERENCES')[0].split(/[(), ]+/).includes(c.column_name));const keys=[pk?'PK':null,fk?'FK':null].filter(Boolean).join(',');diagram+='        '+c.data_type.replaceAll(' ','_')+' '+c.column_name+(keys?' '+keys:'')+'\n'}diagram+='    }\n'}
for(const relation of relationships)diagram+='    '+relation.parent+' '+(relation.required?'||':'|o')+'--'+(relation.child_unique?'o|':'o{')+' '+relation.child+' : "'+relation.name.replace(/^fk_/,'')+'"\n'
writeFileSync('docs/diagrams/relational-model.mmd',diagram)
const schema=execFileSync('docker',[...compose,'pg_dump','-U','mvp_validation','-d',database,'--schema-only','--no-owner','--no-acl'],{encoding:'utf8',maxBuffer:8*1024*1024})
writeFileSync('docs/diagrams/physical-schema.sql',schema.trimEnd()+'\n')
console.log(JSON.stringify({tables:tables.length,businessTables:counts.filter(t=>t.minimum).length,belowMinimum:counts.filter(t=>t.minimum&&t.rows<t.minimum),constraints:constraints.length,indexes:indexes.length,successfulMigrations:migrations.filter(m=>m.success).length}))
if(counts.some(t=>t.minimum&&t.rows<t.minimum)||tables.length!==25||constraints.some(c=>!c.validated))process.exitCode=1
