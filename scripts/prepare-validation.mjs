import {randomBytes} from 'node:crypto'
import {existsSync,writeFileSync} from 'node:fs'
if(existsSync('.env.validation')){
 console.log('Existing private validation environment preserved.')
}else{
 writeFileSync('.env.validation','MVP_DB_PASSWORD='+randomBytes(24).toString('hex')+'\nMVP_JWT_SECRET='+randomBytes(48).toString('hex')+'\n',{mode:0o600})
 console.log('Private isolated validation settings created; values were not printed.')
}
