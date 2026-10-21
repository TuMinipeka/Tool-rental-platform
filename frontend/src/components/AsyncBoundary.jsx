import { Component } from 'react'

export class AsyncBoundary extends Component {
  state = { failed: false }
  static getDerivedStateFromError() { return { failed: true } }
  render() {
    const Element = this.props.as || 'main'
    if (this.state.failed) return <Element className="empty-state page-shell page-top"><div role="alert">
      <h1>No pudimos abrir este contenido.</h1>
      <p>Recarga la página para volver a intentarlo.</p>
      <button className="button button-dark" onClick={() => window.location.reload()}>Recargar página</button>
    </div></Element>
    return this.props.children
  }
}
