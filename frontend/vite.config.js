import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, '.', '')

  return {
    plugins: [react()],
    server: {
      host: true,
      port: Number(env.FRONTEND_PORT || 5173),
      proxy: env.VITE_API_BASE_URL
        ? undefined
        : {
            '/api': { target: 'http://localhost:8080', changeOrigin: true },
            '/status': { target: 'http://localhost:8080', changeOrigin: true },
          },
    },
  }
})
