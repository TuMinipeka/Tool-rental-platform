import { createPointerMotion } from './lib/pointerMotion.js'
import { AsyncBoundary } from './components/AsyncBoundary.jsx'
import { heroImageSizes, heroImageSource, imageAttributes } from './lib/responsiveImages.js'
import { usePointerFrame } from './hooks/usePointerFrame.js'
import { lazy, Suspense, useEffect, useMemo, useRef, useState } from 'react'
import { api, normalizeCatalogTool, readSession, saveSession } from './api.js'
import { demoCatalog } from './demoCatalog.js'
import { prioritizeFeaturedTools } from './toolVisuals.js'

import { Icon } from './components/Icon.jsx'
import { Brand } from './components/Brand.jsx'
import { EmptyState } from './components/EmptyState.jsx'
import { ToolVisual } from './components/ToolVisual.jsx'
import { navigate } from './lib/navigation.js'
import { money } from './lib/format.js'
import { serviceLocations } from './lib/locations.js'

const PaymentReturnPage = lazy(() => import('./pages/PaymentReturnPage.jsx').then(module => ({ default: module.PaymentReturnPage })))
const TermsAndConditionsPage = lazy(() => import('./TermsAndConditionsPage.jsx'))
const loadAccountPages = () => import('./pages/AccountPages.jsx')
const AuthPage = lazy(() => loadAccountPages().then(module => ({ default: module.AuthPage })))
const AuthModal = lazy(() => loadAccountPages().then(module => ({ default: module.AuthModal })))
const AccountPage = lazy(() => loadAccountPages().then(module => ({ default: module.AccountPage })))
const BusinessDashboard = lazy(() => loadAccountPages().then(module => ({ default: module.BusinessDashboard })))
const ProviderEntryPage = lazy(() => loadAccountPages().then(module => ({ default: module.ProviderEntryPage })))
const QuotePage = lazy(() => import('./pages/RentalPages.jsx').then(module => ({ default: module.QuotePage })))
const PaymentPage = lazy(() => import('./pages/RentalPages.jsx').then(module => ({ default: module.PaymentPage })))
const SuccessPage = lazy(() => import('./pages/RentalPages.jsx').then(module => ({ default: module.SuccessPage })))
const AboutPage = lazy(() => import('./pages/InformationPages.jsx').then(module => ({ default: module.AboutPage })))
const HowItWorksPage = lazy(() => import('./pages/InformationPages.jsx').then(module => ({ default: module.HowItWorksPage })))
const InfoPage = lazy(() => import('./pages/InformationPages.jsx').then(module => ({ default: module.InfoPage })))
const ErrorPage = lazy(() => import('./components/ErrorPage.jsx').then(module => ({ default: module.ErrorPage })))
function RouteLoading() { return <main className="route-loading page-shell page-top" role="status" aria-live="polite">Cargando contenido…</main> }

const tools = demoCatalog
const requireLiveApi = import.meta.env.VITE_REQUIRE_LIVE_API === 'true'

const toDateInput=value=>{
  const date=new Date(value)
  const offset=date.getTimezoneOffset()*60000
  return new Date(date.getTime()-offset).toISOString().slice(0,10)
}
const nextDay=new Date()
nextDay.setDate(nextDay.getDate()+1)
const fourthDay=new Date()
fourthDay.setDate(fourthDay.getDate()+4)
const defaultStartDate = toDateInput(nextDay)
const defaultReturnDate = toDateInput(fourthDay)

const storedLocation = typeof window === 'undefined' ? null : window.localStorage.getItem('tool-rental-location')
const requestedLocation = typeof window === 'undefined' ? null : new URLSearchParams(window.location.search).get('municipio')
const defaultLocation = serviceLocations.includes(requestedLocation) ? requestedLocation : serviceLocations.includes(storedLocation) ? storedLocation : serviceLocations[0]
const featuredCategories = [
  { category: 'Demolición', description: 'Rompedores, demoledores y más.', icon: 'hammer' },
  { category: 'Concreto', description: 'Mezcladoras, vibradores y equipos.', icon: 'mixer' },
  { category: 'Corte', description: 'Cortadoras, sierras y discos.', icon: 'saw' },
  { category: 'Limpieza', description: 'Aspiradoras e hidrolavadoras.', icon: 'cleaning' },
  { category: 'Energía', description: 'Generadores y plantas.', icon: 'bolt' },
]

function Header({session,onOpenAuth,path,location,onLocationChange}){
  const [open,setOpen]=useState(false); const go=(path)=>{setOpen(false);navigate(path)}
  const active=target=>path===target||path.startsWith(`${target}/`)
  const initials=(session?.displayName||session?.email||'Mi cuenta').split(/\s+/).slice(0,2).map(part=>part[0]).join('').toUpperCase()
  return <header className="site-header"><div className="nav-shell"><Brand/><nav id="main-navigation" className={`nav-links ${open?'open':''}`}><button className={active('/catalogo')?'active':''} aria-current={active('/catalogo')?'page':undefined} onClick={()=>go('/catalogo')}>Catálogo</button><button className={active('/como-funciona')?'active':''} aria-current={active('/como-funciona')?'page':undefined} onClick={()=>go('/como-funciona')}>Cómo funciona</button><button className={active('/nosotros')?'active':''} aria-current={active('/nosotros')?'page':undefined} onClick={()=>go('/nosotros')}>Sobre nosotros</button>{session&&<button className={active('/cuenta')?'active':''} aria-current={active('/cuenta')?'page':undefined} onClick={()=>go('/cuenta')}>Mi cuenta</button>}</nav><div className="nav-actions"><label className="location-action" aria-label="Municipio de servicio"><Icon name="pin" size={17}/><select value={location} onChange={event=>onLocationChange(event.target.value)}>{serviceLocations.map(city=><option key={city}>{city}</option>)}</select></label>{session?<button className="avatar-button" onClick={()=>navigate('/cuenta')} aria-label="Abrir mi cuenta">{initials}</button>:<button className="login-action" onPointerEnter={loadAccountPages} onFocus={loadAccountPages} onClick={onOpenAuth}><Icon name="user" size={18}/> Ingresar</button>}<button className="publish-action" onClick={()=>go('/publicar')}>Publica tu herramienta <Icon name="arrow" size={17}/></button><button className="menu-action" onClick={()=>setOpen(!open)} aria-label={open?'Cerrar menú':'Abrir menú'} aria-expanded={open} aria-controls="main-navigation"><Icon name={open?'close':'menu'}/></button></div></div></header>
}
function Footer(){return <footer className="site-footer"><div className="footer-lead"><Brand/><h2>Tu proyecto empieza con la herramienta correcta.</h2><button className="button button-lime" onClick={()=>navigate('/catalogo')}>Explorar catálogo <Icon name="arrow"/></button></div><div className="footer-links"><div><strong>Plataforma</strong><button onClick={()=>navigate('/catalogo')}>Catálogo</button><button onClick={()=>navigate('/como-funciona')}>Cómo funciona</button><button onClick={()=>navigate('/cuenta')}>Mi cuenta</button></div><div><strong>Compañía</strong><button onClick={()=>navigate('/nosotros')}>Sobre nosotros</button><button onClick={()=>navigate('/informacion/contacto')}>Contacto</button><button onClick={()=>navigate('/informacion/faq')}>Preguntas frecuentes</button></div><div><strong>Legal</strong><button onClick={()=>navigate('/terms-and-conditions')}>Términos y condiciones</button><button onClick={()=>navigate('/informacion/privacidad')}>Privacidad</button></div></div><div className="footer-bottom"><span>© 2026 Tool Rental · Proyecto académico</span><span>Hecho para construir en Colombia</span></div></footer>}

function SearchBar({location,onLocationChange}){const [term,setTerm]=useState('');return <form className="search-bar" onSubmit={event=>{event.preventDefault();const params=new URLSearchParams();if(term.trim())params.set('q',term.trim());params.set('municipio',location);navigate(`/catalogo?${params}`)}}><div><Icon name="search"/><label htmlFor="home-search">¿Qué necesitas?</label><input id="home-search" value={term} onChange={event=>setTerm(event.target.value)} placeholder="Ej. rotomartillo, mezcladora, cortadora..."/></div><i/><label className="search-location"><Icon name="pin"/><span><small>Ubicación</small><select value={location} onChange={event=>onLocationChange(event.target.value)}>{serviceLocations.map(city=><option key={city} value={city}>{city}, Santander</option>)}</select></span></label><button aria-label="Buscar"><span>Buscar</span><Icon name="arrow"/></button></form>}
function HeroArtwork({location}){
  const imageRef=useRef(null)
  const ringRef=useRef(null)
  const motion=usePointerFrame((_,x,y)=>{
    if(imageRef.current)imageRef.current.style.transform=`translate3d(${x*18}px,${y*14}px,0) rotate(-1deg) scale(1.12,.96)`
    if(ringRef.current)ringRef.current.style.transform=`translate3d(${x*-24}px,${y*-18}px,0) rotate(${x*5}deg)`
  },()=>{
    if(imageRef.current)imageRef.current.style.transform='translate3d(0,0,0) rotate(-1deg) scale(1.12,.96)'
    if(ringRef.current)ringRef.current.style.transform='translate3d(0,0,0) rotate(0deg)'
  })
  return <div className="hero-artwork" {...motion} aria-hidden="true"><div className="hero-orbit" ref={ringRef}><span/><span/><span/></div><div className="hero-crosshair"/><img ref={imageRef} {...imageAttributes(heroImageSource, heroImageSizes)} alt="" fetchPriority="high"/><div className="hero-spec hero-spec-top"><small>CLASE</small><strong>PRO / 01</strong></div><div className="hero-spec hero-spec-bottom"><small>DISPONIBLE EN</small><strong><i/> {location.toUpperCase()}</strong></div><span className="hero-side-copy">HERRAMIENTAS<br/>QUE HACEN<br/>PROYECTOS<br/>REALES</span><span className="hero-coordinate">7.1193° N · 73.1227° W</span></div>
}
function FeaturedToolCard({tool,index}){const openTool=()=>navigate(`/herramienta/${tool.id}`);return <a href={`/herramienta/${tool.id}`} className="featured-tool-card" aria-label={`Ver ${tool.name}`} onClick={event=>{if(event.button===0&&!event.metaKey&&!event.ctrlKey&&!event.shiftKey&&!event.altKey){event.preventDefault();openTool()}}} onKeyDown={event=>{if(event.key===' '){event.preventDefault();openTool()}}}><div className="featured-tool-media"><ToolVisual tool={tool} compact reuseHero/>{index===0&&<span className="featured-tool-badge">Destacado</span>}</div><div className="featured-tool-body"><p>{tool.category}</p><h3>{tool.name}</h3><div className="featured-tool-meta"><span>{tool.location?.split(',')[0]||'Ubicación por coordinar'}</span><span>{tool.stock>0?`${tool.stock} ${tool.stock===1?'unidad':'unidades'}`:'Sin existencias'}</span></div><div className="featured-tool-price"><strong>{money.format(tool.price)}</strong><span>/ día</span></div><div className={`featured-tool-status ${tool.stock>0?'available':'unavailable'}`}><i/><span>{tool.stock>0?'Con unidades operativas':'Sin unidades operativas'}</span><Icon name="arrow" size={17}/></div></div></a>}
function FeaturedSection({catalogTools,dataMode}){const pageSize=5;const [startIndex,setStartIndex]=useState(0);const orderedTools=useMemo(()=>prioritizeFeaturedTools(catalogTools),[catalogTools]);const lastPageStart=Math.max(0,Math.floor((orderedTools.length-1)/pageSize)*pageSize);const visibleStart=Math.min(startIndex,lastPageStart);const visibleTools=orderedTools.slice(visibleStart,visibleStart+pageSize);return <section id="destacados" className="featured-section" data-reveal><div className="page-shell featured-shell"><div className="featured-heading"><div><p className="kicker dark"><span/> Equipos disponibles ahora</p><h2>Equipos destacados</h2><p>{dataMode==='api'?'Selección construida con publicaciones recibidas del catálogo público.':'Muestra demostrativa para revisar el diseño antes de conectar publicaciones reales.'}</p></div><div className="featured-actions"><button className="text-link" onClick={()=>navigate('/catalogo')}>Ver todos los equipos <Icon name="arrow"/></button><button className="featured-arrow" aria-label="Ver equipos anteriores" disabled={visibleStart===0} onClick={()=>setStartIndex(Math.max(0,visibleStart-pageSize))}>←</button><button className="featured-arrow" aria-label="Ver equipos siguientes" disabled={visibleStart>=lastPageStart} onClick={()=>setStartIndex(Math.min(lastPageStart,visibleStart+pageSize))}>→</button></div></div>{dataMode==='demo'&&<p className="featured-disclosure">Contenido demostrativo: precios, ubicaciones y unidades son provisionales.</p>}<div className="featured-grid" aria-live="polite">{visibleTools.map((tool,index)=><FeaturedToolCard key={tool.id} tool={tool} index={index}/>)}</div></div></section>}
function HomePage({catalogTools,dataMode,location,onLocationChange}){return <main>
  <section className="home-hero"><div className="hero-noise"/><div className="hero-content page-shell"><div className="hero-copy"><p className="kicker"><span/> Equipos profesionales, sin comprarlos</p><h1><span className="hero-line"><b>Haz</b> más.</span><span className="hero-line hero-line-accent">Compra <em>menos.</em></span></h1><p className="hero-subtitle">Encuentra herramientas cerca de tu proyecto, consulta fechas y crea una solicitud trazable.</p><div className="hero-actions"><button className="button button-lime" onClick={()=>navigate('/catalogo')}>Explorar equipos <Icon name="arrow"/></button><button className="hero-secondary" onClick={()=>navigate('/como-funciona')}>Cómo funciona <span>↗</span></button></div><div className="trust-row"><span><Icon name="calendar" size={20}/><b>Fechas inclusivas<small>Cálculo consistente</small></b></span><span><Icon name="shield" size={20}/><b>Sesiones protegidas<small>Renovación segura</small></b></span><span><Icon name="box" size={20}/><b>Inventario consultable<small>Datos del catálogo</small></b></span><span><Icon name="check" size={20}/><b>Estados trazables<small>Seguimiento real</small></b></span></div></div><HeroArtwork location={location}/><div className="hero-search"><SearchBar location={location} onLocationChange={onLocationChange}/></div></div><div className="scroll-note"><span/> DESLIZA PARA EXPLORAR</div></section>
  <div className="industrial-marquee" aria-hidden="true"><div>PERFORA <i/> CONSTRUYE <i/> LIMPIA <i/> ELEVA <i/> REPARA <i/> PERFORA <i/> CONSTRUYE <i/> LIMPIA <i/> ELEVA <i/> REPARA <i/></div></div>
  <section className="category-section page-shell" data-reveal><div className="section-title-row"><div><p className="kicker dark"><span/> Encuentra lo que buscas</p><h2>Herramientas para<br/><em>cada proyecto.</em></h2></div><button className="text-link" onClick={()=>navigate('/catalogo')}>Ver todo el catálogo <Icon name="arrow"/></button></div><div className="category-tiles">{featuredCategories.map(({category,description,icon},index)=>{const count=catalogTools.filter(tool=>tool.category===category).length;return <button key={category} onClick={()=>navigate(`/catalogo?categoria=${encodeURIComponent(category)}&municipio=${encodeURIComponent(location)}`)} title={`${category}: ${dataMode==='api'?`${count} equipos disponibles`:'explorar muestra'}`}><span className={`category-icon category-${index+1}`}><Icon name={icon} size={42}/></span><span className="category-index" aria-hidden="true">0{index+1}</span><strong>{category}</strong><small>{description}</small><Icon name="arrow"/></button>})}</div></section>
  <FeaturedSection catalogTools={catalogTools} dataMode={dataMode}/>
  <section className="steps-section page-shell" data-reveal><div className="steps-intro"><p className="kicker dark"><span/> Así de simple</p><h2>Cómo funciona.</h2><p>La reserva retiene la unidad mientras completas el checkout de prueba en Mercado Pago.</p></div><div className="steps-list">{[['01','Busca','Explora el catálogo y consulta las fechas.'],['02','Solicita','Elige recogida o entrega y crea la solicitud.'],['03','Confirma','Paga con una cuenta de prueba; el servidor verifica y confirma la reserva.'],['04','Haz más','Sigue entrega, uso y devolución desde tu cuenta.']].map(x=><article key={x[0]}><span>{x[0]}</span><div><h3>{x[1]}</h3><p>{x[2]}</p></div><Icon name="arrow"/></article>)}</div></section>
  <section className="supplier-cta" data-reveal><div className="page-shell"><p className="kicker"><span/> Para proveedores</p><h2>Tu equipo puede<br/><em>seguir produciendo.</em></h2><p>Conoce el proceso para operar como proveedor y administrar únicamente tus propios recursos.</p><button className="button button-light" onClick={()=>navigate('/publicar')}>Publica tu herramienta <Icon name="arrow"/></button></div><div className="cta-orbit" aria-hidden="true"><span>PUBLICA</span><b>ACTIVA</b><small>ADMINISTRA</small></div></section>
  </main>}
const toolCardMotion=createPointerMotion((node,x,y)=>{node.style.transform=`perspective(900px) rotateX(${y*-3}deg) rotateY(${x*4}deg) translateY(-6px)`},node=>{node.style.transform=""})
function ToolCard({tool}){const openTool=()=>navigate(`/herramienta/${tool.id}`);return <article className="tool-card catalog-tool-card" {...toolCardMotion} onClick={event=>{if(!event.target.closest('button,a'))openTool()}}><div className="tool-card-media"><ToolVisual tool={tool} compact/><span className="tool-category-label">{tool.category}</span><button className="tool-favorite" type="button" aria-label="Favoritos disponibles próximamente" onClick={event=>event.stopPropagation()}><Icon name="heart" size={18}/></button></div><div className="tool-card-body"><h3><a className="tool-title-link" href={`/herramienta/${tool.id}`} onClick={event=>{if(event.button===0&&!event.metaKey&&!event.ctrlKey&&!event.shiftKey&&!event.altKey){event.preventDefault();openTool()}}}>{tool.name}</a></h3><div className="tool-facts"><span><Icon name="box" size={14}/>{tool.stock>0?`${tool.stock} ${tool.stock===1?'unidad':'unidades'}`:'Sin existencias'}</span><span><Icon name="pin" size={14}/>{tool.location?.split(',')[0]||'Ubicación por coordinar'}</span></div><div className="tool-price"><span><strong>{money.format(tool.price)}</strong> / día</span><button type="button" onClick={openTool}>Ver detalle <Icon name="arrow" size={15}/></button></div></div></article>}

function CatalogPage({catalogTools,dataMode,location,onLocationChange}){
  const params=new URLSearchParams(window.location.search)
  const [query,setQuery]=useState(params.get('q')||'')
  const [category,setCategory]=useState(params.get('categoria')||'Todas')
  const [sort,setSort]=useState('Recomendados')
  const [page,setPage]=useState(1)
  const [maxPrice,setMaxPrice]=useState(230000)
  const availableCategories=['Todas',...new Set(catalogTools.map(tool=>tool.category))]
  useEffect(()=>{const next=new URLSearchParams();if(query.trim())next.set('q',query.trim());if(category!=='Todas')next.set('categoria',category);next.set('municipio',location);window.history.replaceState({},'',`/catalogo?${next}`)},[query,category,location])
  const visible=useMemo(()=>catalogTools.filter(tool=>(category==='Todas'||tool.category===category)&&tool.price<=maxPrice&&`${tool.name} ${tool.category}`.toLowerCase().includes(query.toLowerCase())).sort((a,b)=>sort==='Menor precio'?a.price-b.price:sort==='Mayor precio'?b.price-a.price:0),[catalogTools,query,category,sort,maxPrice])
  const pageSize=10
  const totalPages=Math.max(1,Math.ceil(visible.length/pageSize))
  const currentPage=Math.min(page,totalPages)
  const pageTools=visible.slice((currentPage-1)*pageSize,currentPage*pageSize)
  return <main className="catalog-page catalog-reference-page">
    <section className="catalog-hero"><div className="catalog-blueprint"/><div className="catalog-hero-shell page-shell"><div><p className="kicker"><span/> Equipos profesionales a tu alcance</p><h1>Catálogo de herramientas</h1><p>Encuentra equipos para tus proyectos en {location} y su área metropolitana. Alquila, trabaja y haz más.</p></div><img src="/assets/hero-rotary-hammer-1200.png" alt=""/><span className="catalog-side-copy">HERRAMIENTAS<br/>QUE HACEN<br/>PROYECTOS<br/>REALES</span></div></section>
    <div className="catalog-search-dock page-shell"><label><Icon name="search"/><span><small>¿Qué necesitas?</small><input aria-label="Buscar herramienta o categoría" value={query} onChange={event=>{setQuery(event.target.value);setPage(1)}} placeholder="Ej. rotomartillo, mezcladora, cortadora..."/></span></label><label><Icon name="pin"/><span><small>Ubicación</small><select value={location} onChange={event=>{onLocationChange(event.target.value);setPage(1)}}>{serviceLocations.map(city=><option key={city} value={city}>{city}, Santander</option>)}</select></span></label><label><Icon name="grid"/><span><small>Categoría</small><select value={category} onChange={event=>{setCategory(event.target.value);setPage(1)}}>{availableCategories.map(item=><option key={item} value={item}>{item==='Todas'?'Todas las categorías':item}</option>)}</select></span></label><label><Icon name="calendar"/><span><small>Disponibilidad</small><input type="date" min={defaultStartDate} aria-label="Fecha de disponibilidad"/></span></label><button type="button" onClick={()=>setQuery(query.trim())}>Buscar <Icon name="arrow"/></button></div>
    <div className="catalog-body page-shell"><nav className="catalog-breadcrumb" aria-label="Ruta de navegación"><button onClick={()=>navigate('/')}>Inicio</button><span>›</span><strong>Catálogo</strong></nav>{dataMode==='demo'&&<div className="catalog-demo-label" role="status">Catálogo demostrativo · datos provisionales</div>}
      <aside className="catalog-filters"><h2>Filtros</h2><div className="filter-section"><strong>Categorías</strong>{availableCategories.map(item=>{const count=item==='Todas'?catalogTools.length:catalogTools.filter(tool=>tool.category===item).length;return <label key={item}><input type="checkbox" checked={category===item} onChange={()=>{setCategory(item);setPage(1)}}/><span>{item==='Todas'?'Todas las categorías':item}</span><small>{count}</small></label>})}</div><div className="filter-section"><strong>Precio por día</strong><input type="range" min="0" max="230000" step="5000" value={maxPrice} onChange={event=>{setMaxPrice(Number(event.target.value));setPage(1)}} aria-label="Precio diario máximo"/><div className="filter-range"><span>$ 0</span><span>{money.format(maxPrice)}</span></div></div><div className="filter-section filter-info"><strong>Información verificada</strong><p>Los filtros corresponden únicamente a campos expuestos por el catálogo.</p></div></aside>
      <section className="catalog-results"><h2 className="sr-only">Resultados del catálogo</h2><div className="catalog-toolbar"><strong>{visible.length} {visible.length===1?'herramienta encontrada':'herramientas encontradas'}</strong><label>Ordenar por <select value={sort} onChange={event=>{setSort(event.target.value);setPage(1)}}><option>Recomendados</option><option>Menor precio</option><option>Mayor precio</option></select></label></div>{pageTools.length?<div className="catalog-grid">{pageTools.map(tool=><ToolCard key={tool.id} tool={tool}/>)}</div>:<EmptyState as="section" title="No encontramos coincidencias" text="Prueba otra palabra o elimina los filtros." action={()=>{setQuery('');setCategory('Todas');setMaxPrice(230000);setPage(1)}}/>}{totalPages>1&&<nav className="catalog-pagination" aria-label="Paginación"><button disabled={currentPage===1} onClick={()=>setPage(value=>Math.max(1,value-1))}>←</button>{Array.from({length:totalPages},(_,index)=>index+1).map(number=><button key={number} className={number===currentPage?'active':''} aria-current={number===currentPage?'page':undefined} onClick={()=>setPage(number)}>{number}</button>)}<button disabled={currentPage===totalPages} onClick={()=>setPage(value=>Math.min(totalPages,value+1))}>→</button></nav>}</section>
    </div>
  </main>
}
function DateField({label,value,onChange,min}){return <label className="date-field"><span>{label}</span><div><Icon name="calendar" size={18}/><input type="date" value={value} min={min} onChange={e=>onChange(e.target.value)}/></div></label>}
function getDays(start,end){if(!start||!end)return 1;return Math.max(1,Math.round((new Date(`${end}T12:00:00`)-new Date(`${start}T12:00:00`))/86400000)+1)}
function ToolDetailPage({id,onReserve,catalogTools,dataMode}){const tool=catalogTools.find(item=>item.id===id)||catalogTools[0]||tools[0];const [startDate,setStartDate]=useState(defaultStartDate);const [returnDate,setReturnDate]=useState(defaultReturnDate);const days=getDays(startDate,returnDate);const hasStock=tool.stock>0;if(requireLiveApi&&!catalogTools.some(item=>item.id===id))return <EmptyState title="Herramienta no disponible" text="La herramienta no figura en el catálogo actual del servidor." action={()=>navigate('/catalogo')} actionText="Volver al catálogo"/>;return <main className="detail-page page-shell page-top"><nav className="detail-breadcrumb" aria-label="Ruta de navegación"><button onClick={()=>navigate('/')}>Inicio</button><span>/</span><button onClick={()=>navigate('/catalogo')}>Catálogo</button><span>/</span><button onClick={()=>navigate(`/catalogo?categoria=${encodeURIComponent(tool.category)}`)}>{tool.category}</button><span>/</span><strong>{tool.name}</strong></nav>{dataMode==='demo'&&<div className="data-notice" role="status">Ficha demostrativa: esta herramienta, su precio, ubicación y unidades no son una publicación real.</div>}<div className="detail-layout"><section className="detail-gallery" aria-label="Galería de herramienta"><div className="thumbnail-rail"><button className="active" aria-label="Ver imagen principal"><ToolVisual tool={tool} compact/></button><button disabled title="No hay más imágenes publicadas">+0</button></div><div className="detail-main-visual"><ToolVisual tool={tool} priority/>{!tool.imageUrl&&<span className="placeholder-note">Representación visual · sin fotografía del proveedor</span>}</div></section><section className="detail-info"><p className="detail-overline">{tool.category} <span/> {hasStock?'Con unidades registradas':'Sin existencias'}</p><h1>{tool.name}</h1><p className="detail-description-copy">{tool.description}</p><div className="detail-specs"><span><Icon name="box"/><strong>{tool.stock}</strong><small>unidades operativas</small></span><span><Icon name="pin"/><strong>{tool.location?.split(',')[0]||'Por coordinar'}</strong><small>ubicación publicada</small></span><span><Icon name="calendar"/><strong>Inclusivos</strong><small>días facturables</small></span></div><div className="detail-integrity"><Icon name="shield"/><div><strong>Información disponible en el catálogo</strong><p>No mostramos reseñas, certificaciones ni verificación del proveedor porque la API pública actual no las acredita.</p></div></div></section><aside className="booking-card"><div className="booking-price"><strong>{money.format(tool.price)}</strong><span>/ día</span></div><div className={`availability-badge ${hasStock?'':'unavailable'}`}><span/><strong>{hasStock?`${tool.stock} ${tool.stock===1?'unidad operativa':'unidades operativas'}`:'Sin unidades operativas'}</strong></div><DateField label="Fecha de inicio" value={startDate} min={defaultStartDate} onChange={value=>{setStartDate(value);if(value>returnDate)setReturnDate(value)}}/><DateField label="Fecha de devolución" value={returnDate} min={startDate} onChange={setReturnDate}/><div className="booking-summary"><span>{money.format(tool.price)} × {days} {days===1?'día':'días'}</span><strong>{money.format(tool.price*days)}</strong><span>Entrega e impuestos</span><strong>Por calcular</strong></div><div className="booking-total"><span>Subtotal estimado</span><strong>{money.format(tool.price*days)}</strong></div><button className="button button-lime wide" disabled={!hasStock} onClick={()=>onReserve({tool,startDate,returnDate,days})}>{hasStock?'Comprobar disponibilidad':'No disponible'} <Icon name="arrow"/></button><small className="booking-note">La disponibilidad y el total se validan nuevamente en el backend. No se realizará ningún cobro.</small></aside></div><section className="fulfillment-preview"><article><Icon name="pin"/><div><strong>Recogida con el proveedor</strong><p>El punto se coordina después de que la solicitud sea confirmada.</p></div></article><article><Icon name="box"/><div><strong>Entrega del proveedor</strong><p>Si está disponible, la tarifa versionada se calcula antes de crear la solicitud.</p></div></article><div className="detail-benefits"><span><Icon name="calendar"/>Fechas trazables</span><span><Icon name="shield"/>Sesión protegida</span></div></section><section className="detail-technical"><p className="kicker"><span/> Lo que sí sabemos</p><h2>Información para decidir,<br/><em>sin inventar atributos.</em></h2><div><span><strong>{tool.category}</strong><small>Categoría</small></span><span><strong>{money.format(tool.price)}</strong><small>Tarifa diaria publicada</small></span><span><strong>{tool.stock}</strong><small>Unidades operativas</small></span></div></section></main>}





function App(){
  const [path,setPath]=useState(window.location.pathname)
  const [session,setSession]=useState(readSession())
  const [authOpen,setAuthOpen]=useState(false)
  const [reservation,setReservation]=useState(null)
  const [catalogTools,setCatalogTools]=useState(requireLiveApi ? [] : tools)
  const [dataMode,setDataMode]=useState(requireLiveApi ? 'loading' : 'demo')
  const [location,setLocation]=useState(defaultLocation)
  const displayedCatalogTools=useMemo(()=>catalogTools.filter(tool=>tool.location===location),[catalogTools,location])

  const changeLocation=nextLocation=>{
    if(!serviceLocations.includes(nextLocation))return
    setLocation(nextLocation)
    window.localStorage.setItem('tool-rental-location',nextLocation)
    const currentUrl=new URL(window.location.href)
    if(currentUrl.pathname==='/'||currentUrl.pathname==='/catalogo'){
      currentUrl.searchParams.set('municipio',nextLocation)
      window.history.replaceState({},'',`${currentUrl.pathname}?${currentUrl.searchParams}`)
    }
  }

  useEffect(()=>{
    const update=()=>{
      setPath(window.location.pathname)
      const requestedLocation=new URLSearchParams(window.location.search).get('municipio')
      if(serviceLocations.includes(requestedLocation)){
        setLocation(requestedLocation)
        window.localStorage.setItem('tool-rental-location',requestedLocation)
      }
    }
    const open=()=>navigate(`/login?returnTo=${encodeURIComponent(window.location.pathname)}`)
    const updateSession=event=>setSession(event.detail)
    window.addEventListener('popstate',update)
    window.addEventListener('open-auth',open)
    window.addEventListener('tool-rental-session',updateSession)
    api.restoreSession().catch(()=>null)
    api.listCatalog().then(items=>{
      if(items.length || requireLiveApi){setCatalogTools(items.map(normalizeCatalogTool));setDataMode(import.meta.env.VITE_DEMO_MODE==='true'?'demo':'api')}
    }).catch(()=>{if(requireLiveApi){setCatalogTools([]);setDataMode('error')}else setDataMode('demo')})
    return()=>{window.removeEventListener('popstate',update);window.removeEventListener('open-auth',open);window.removeEventListener('tool-rental-session',updateSession)}
  },[])

  useEffect(()=>{
    const elements=[...document.querySelectorAll('[data-reveal]')]
    if(window.matchMedia('(prefers-reduced-motion: reduce)').matches){elements.forEach(element=>element.classList.add('is-visible'));return undefined}
    const observer=new IntersectionObserver(entries=>entries.forEach(entry=>{if(entry.isIntersecting){entry.target.classList.add('is-visible');observer.unobserve(entry.target)}}),{rootMargin:'0px 0px -8% 0px',threshold:.12})
    elements.forEach(element=>observer.observe(element))
    return()=>observer.disconnect()
  },[path,catalogTools.length])

  const reserve=async selection=>{
    if(selection.tool.source==='api'){
      try{
        const availability=await api.getAvailability(selection.tool.id,selection.startDate,selection.returnDate)
        selection={...selection,equipmentUnitIds:availability.availableEquipmentUnitIds}
      }catch(error){selection={...selection,availabilityError:error.message}}
    }
    setReservation(selection)
    navigate('/cotizacion')
  }
  const login=authenticated=>{
    saveSession(authenticated)
    setSession(authenticated)
    setAuthOpen(false)
  }
  const logout=async()=>{
    await api.logout().catch(()=>null)
    saveSession(null);setSession(null);navigate('/')
  }
  const continueQuote=async logistics=>{
    if(!session){navigate('/login?returnTo=%2Fcotizacion');return}
    if(reservation?.tool.source==='api'&&reservation.equipmentUnitIds?.length&&!session?.demo){
      try{
        const equipmentUnitIds=[reservation.equipmentUnitIds[0]]
        const quote=await api.quoteReservation({startDate:reservation.startDate,returnDate:reservation.returnDate,equipmentUnitIds,fulfillmentMethod:logistics.fulfillmentMethod,deliveryCountryCode:logistics.deliveryCountryCode})
        const created=await api.createReservation({startDate:reservation.startDate,returnDate:reservation.returnDate,equipmentUnitIds,...logistics,scheduledFulfillmentAt:new Date(logistics.scheduledFulfillmentAt).toISOString()})
        setReservation(current=>({...current,quote,reservationId:created.id?.value||created.id,reservationStatus:created.status,expiresAt:created.expiresAt,serverTotal:created.total}))
      }catch(error){setReservation(current=>({...current,apiError:error.message}));return}
    }
    navigate('/reserva-confirmada')
  }
  let page
  if(path==='/')page=<HomePage catalogTools={displayedCatalogTools} dataMode={dataMode} location={location} onLocationChange={changeLocation}/>
  else if(path==='/catalogo')page=<CatalogPage catalogTools={displayedCatalogTools} dataMode={dataMode} location={location} onLocationChange={changeLocation}/>
  else if(path.startsWith('/herramienta/'))page=<ToolDetailPage id={path.split('/').pop()} onReserve={reserve} catalogTools={catalogTools} dataMode={dataMode}/>
  else if(path==='/cotizacion')page=<QuotePage reservation={reservation} session={session} onContinue={continueQuote}/>
  else if(path==='/pago')page=<PaymentPage reservation={reservation} session={session}/>
  else if(path==='/reserva-confirmada')page=<SuccessPage reservation={reservation}/>
  else if(path==='/payment-return')page=<PaymentReturnPage session={session}/>
  else if(path==='/login')page=<AuthPage mode="login" onAuthenticated={login}/>
  else if(path==='/registro/proveedor')page=<AuthPage mode="supplier" onAuthenticated={login}/>
  else if(path==='/registro')page=<AuthPage mode="register" onAuthenticated={login}/>
  else if(path==='/cuenta')page=<AccountPage session={session} onLogout={logout}/>
  else if(path==='/panel/proveedor')page=<BusinessDashboard role="provider" session={session}/>
  else if(path==='/panel/admin')page=<BusinessDashboard role="admin" session={session}/>
  else if(path==='/publicar')page=<ProviderEntryPage session={session} onOpenAuth={()=>navigate('/login?returnTo=%2Fpublicar')}/>
  else if(path==='/terms-and-conditions')page=<TermsAndConditionsPage/>
  else if(path==='/nosotros')page=<AboutPage/>
  else if(path==='/como-funciona')page=<HowItWorksPage/>
  else if(path.startsWith('/informacion/'))page=<InfoPage type={path.split('/').pop()}/>
  else if(path==='/403')page=<ErrorPage code="403"/>
  else page=<ErrorPage/>
  const minimal=path==='/reserva-confirmada'
  const authRoute=['/login','/registro','/registro/proveedor'].includes(path)
  const footerHidden=authRoute||path==='/pago'||path==='/como-funciona'||path.startsWith('/panel/')||path==='/cuenta'
  return <div className={`app ${path==='/'?'home-route':''}`}><a className="skip-link" href="#main-content">Saltar al contenido</a>{!minimal&&<Header path={path} session={session} onOpenAuth={()=>navigate('/login')} location={location} onLocationChange={changeLocation}/>}<div className="page-content page-enter" id="main-content" tabIndex={-1} key={path}><AsyncBoundary key={path}>{dataMode==='error'&&<p className="data-notice" role="alert">No pudimos cargar el catálogo del servidor. Reintenta en unos minutos; no se mostrarán datos de respaldo.</p>}<Suspense fallback={<RouteLoading/>}>{page}</Suspense></AsyncBoundary></div>{!minimal&&!footerHidden&&<Footer/>}{authOpen&&<AsyncBoundary as="section"><Suspense fallback={<div className="modal-backdrop"><p className="auth-modal" role="status">Cargando acceso…</p></div>}><AuthModal onClose={()=>setAuthOpen(false)} onLogin={login}/></Suspense></AsyncBoundary>}</div>
}
export default App
