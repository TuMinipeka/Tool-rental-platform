const journeys = [
  {
    number: '01',
    title: 'Explora el catálogo',
    description: 'Encuentra equipos por categoría y conoce su tarifa diaria.',
  },
  {
    number: '02',
    title: 'Solicita una reserva',
    description: 'Selecciona fechas y unidades disponibles de un proveedor.',
  },
  {
    number: '03',
    title: 'Recibe y devuelve',
    description: 'Conserva el seguimiento de entrega, devolución y estado.',
  },
]

function App() {
  return (
    <main>
      <nav className="navigation" aria-label="Navegación principal">
        <a className="brand" href="#inicio" aria-label="Tool Rental, inicio">
          <span className="brand-mark" aria-hidden="true">
            TR
          </span>
          <span>Tool Rental</span>
        </a>
        <a className="nav-link" href="#estado">
          Estado del proyecto
        </a>
      </nav>

      <section className="hero" id="inicio">
        <div className="hero-copy">
          <p className="eyebrow">Herramientas listas para el trabajo real</p>
          <h1>El equipo correcto, justo cuando lo necesitas.</h1>
          <p className="hero-description">
            Una plataforma para conectar clientes y proveedores de herramientas de
            construcción con reservas claras y trazables.
          </p>
          <div className="hero-actions">
            <a className="primary-action" href="#recorrido">
              Conocer el recorrido
            </a>
            <span className="availability">Experiencia inicial en construcción</span>
          </div>
        </div>

        <aside className="status-card" id="estado" aria-labelledby="status-title">
          <div className="status-heading">
            <span className="status-dot" aria-hidden="true" />
            <span>Base técnica disponible</span>
          </div>
          <h2 id="status-title">Preparada para crecer sobre una base confiable.</h2>
          <dl>
            <div>
              <dt>Backend</dt>
              <dd>Java 21 + Spring Boot</dd>
            </div>
            <div>
              <dt>Datos</dt>
              <dd>PostgreSQL + Flyway</dd>
            </div>
            <div>
              <dt>Interfaz</dt>
              <dd>React + JavaScript</dd>
            </div>
          </dl>
          <p>
            La API y los recorridos completos se habilitarán cuando sus contratos estén
            aprobados.
          </p>
        </aside>
      </section>

      <section className="journey" id="recorrido" aria-labelledby="journey-title">
        <div className="section-heading">
          <p className="eyebrow">Recorrido previsto</p>
          <h2 id="journey-title">Del catálogo a la devolución.</h2>
        </div>
        <div className="journey-grid">
          {journeys.map((journey) => (
            <article className="journey-card" key={journey.number}>
              <span>{journey.number}</span>
              <h3>{journey.title}</h3>
              <p>{journey.description}</p>
            </article>
          ))}
        </div>
      </section>

      <footer>
        <span>Tool Rental Platform</span>
        <span>Implementación académica · Colombia</span>
      </footer>
    </main>
  )
}

export default App
