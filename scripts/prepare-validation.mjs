import {randomBytes} from 'node:crypto'
import {existsSync,readFileSync,appendFileSync,writeFileSync} from 'node:fs'
if(existsSync('.env.validation')){
 if(!/^MVP_SANDBOX_SECRET=.+$/m.test(readFileSync('.env.validation','utf8')))appendFileSync('.env.validation','\nMVP_SANDBOX_SECRET='+randomBytes(48).toString('hex')+'\n')
 console.log('Existing private validation environment preserved.')
}else{
 writeFileSync('.env.validation','MVP_DB_PASSWORD='+randomBytes(24).toString('hex')+'\nMVP_JWT_SECRET='+randomBytes(48).toString('hex')+'\nMVP_SANDBOX_SECRET='+randomBytes(48).toString('hex')+'\n',{mode:0o600})
 console.log('Private isolated validation settings created; values were not printed.')
}
