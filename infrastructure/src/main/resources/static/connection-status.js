(() => {
  const panel = document.querySelector('.connection-panel')
  const title = document.querySelector('#status-title')
  const label = document.querySelector('#status-label')
  const description = document.querySelector('#status-description')
  const checkedAt = document.querySelector('#checked-at')
  const button = document.querySelector('#recheck')
  const symbol = document.querySelector('.other-symbol')
  let busy = false

  async function checkConnection() {
    if (busy) return
    busy = true
    button.disabled = true
    panel.dataset.state = 'checking'
    panel.setAttribute('aria-busy', 'true')
    label.textContent = 'COMPROBANDO CONEXIÓN'
    title.textContent = 'Comprobando la conexión…'
    description.textContent = 'Estamos consultando la base de datos desde el backend.'
    symbol.textContent = '···'
    const controller = new AbortController()
    const timeout = setTimeout(() => controller.abort(), 6000)
    try {
      const response = await fetch('/status/database', { cache: 'no-store', signal: controller.signal })
      const status = await response.json()
      const connected = response.ok && status.connected === true
      panel.dataset.state = connected ? 'connected' : 'disconnected'
      label.textContent = connected ? 'CONEXIÓN VERIFICADA' : 'CONEXIÓN NO DISPONIBLE'
      title.textContent = connected ? 'Conexión a la base de datos exitosa.' : 'No hay conexión con la base de datos.'
      description.textContent = connected ? 'El backend se comunica correctamente con PostgreSQL. Puedes continuar trabajando en la plataforma.' : 'El backend respondió, pero no pudo consultar PostgreSQL. Revisa el servicio y la configuración de conexión.'
      symbol.textContent = '!'
      const instant = new Date(status.checkedAt)
      checkedAt.dateTime = instant.toISOString()
      checkedAt.textContent = new Intl.DateTimeFormat('es-CO', { dateStyle: 'medium', timeStyle: 'medium', timeZone: 'America/Bogota' }).format(instant) + ' · Bogotá'
    } catch {
      panel.dataset.state = 'disconnected'
      label.textContent = 'SIN VERIFICACIÓN'
      title.textContent = 'No pudimos comprobar la conexión.'
      description.textContent = 'No recibimos una respuesta válida a tiempo. Comprueba que el backend siga en ejecución y vuelve a intentarlo.'
      checkedAt.removeAttribute('datetime')
      checkedAt.textContent = 'No disponible'
      symbol.textContent = '!'
    } finally {
      clearTimeout(timeout)
      busy = false
      button.disabled = false
      panel.setAttribute('aria-busy', 'false')
    }
  }
  button.addEventListener('click', checkConnection)
  setInterval(() => { if (!document.hidden) checkConnection() }, 30000)
  checkConnection()
})()
