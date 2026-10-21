import {chromium} from '@playwright/test'
import {readFileSync,writeFileSync} from 'node:fs'
import {resolve} from 'node:path'
const response=await fetch('https://cdn.jsdelivr.net/npm/mermaid@11.12.0/dist/mermaid.min.js')
if(!response.ok)throw Error('Mermaid library unavailable')
const library=await response.text()
const source=readFileSync('../docs/diagrams/relational-model.mmd','utf8')
const browser=await chromium.launch({channel:'chrome'})
try{
const page=await browser.newPage({viewport:{width:1800,height:1000},deviceScaleFactor:1})
await page.setContent('<html><body style="margin:0;background:white"><div id="diagram"></div></body></html>')
await page.addScriptTag({content:library})
const svg=await page.evaluate(async source=>{window.mermaid.initialize({startOnLoad:false,theme:'neutral',er:{useMaxWidth:false}});const {svg}=await window.mermaid.render('relationalModel',source);document.querySelector('#diagram').innerHTML=svg;return svg},source)
writeFileSync('../docs/diagrams/relational-model.svg',svg)
await page.locator('svg').screenshot({path:resolve('../docs/diagrams/relational-model.png')})
console.log('Rendered relational diagram from exported PostgreSQL keys.')
}finally{await browser.close()}
