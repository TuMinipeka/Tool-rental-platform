import { navigate } from '../lib/navigation.js'

export function Brand({to='/'}){return <button className="brand" onClick={()=>navigate(to)} aria-label="toolrental: ir al inicio"><span className="brand-symbol"><i/><i/><i/></span><span>tool<em>rental</em></span></button>}
