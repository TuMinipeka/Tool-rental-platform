import { Icon } from './Icon.jsx'

export function EmptyState({as:Element="main",title,text,action,actionText='Limpiar filtros'}){return <Element className="empty-state page-shell page-top"><span className="empty-icon"><Icon name="search" size={40}/></span><h1>{title}</h1><p>{text}</p><button className="button button-dark" onClick={action}>{actionText} <Icon name="arrow"/></button></Element>}
