import { Icon } from './Icon.jsx'
import { navigate } from '../lib/navigation.js'
export function ErrorPage({code='404'}){return <main className="error-page"><span>{code}</span><p className="kicker dark"><i/> Algo no salió como esperábamos</p><h1>{code==='403'?'Este espacio no está disponible para tu rol.':'Parece que esta herramienta cambió de lugar.'}</h1><button className="button button-dark" onClick={()=>navigate('/')}>Volver al inicio <Icon name="arrow"/></button></main>}
