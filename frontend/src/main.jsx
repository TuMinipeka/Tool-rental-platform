import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import '@fontsource-variable/archivo'
import '@fontsource-variable/public-sans'
import App from './App.jsx'
import { releaseBootSplashWhenRendered } from './lib/bootSplash.js'
import './styles.css'
import './premium.css'
import './reference-pages.css'

const rootElement = document.getElementById('root')

createRoot(rootElement).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
releaseBootSplashWhenRendered(rootElement)
