import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/form-api': {
        target: 'http://localhost:8090',
        changeOrigin: true,
        credentials: 'include'
      },
      '/process-api': {
        target: 'http://localhost:8090',
        changeOrigin: true,
        credentials: 'include'
      },
      '/login': {
        target: 'http://localhost:8090',
        changeOrigin: true,
        credentials: 'include'
      },
      '/logout': {
        target: 'http://localhost:8090',
        changeOrigin: true,
        credentials: 'include'
      },
      '/health': {
        target: 'http://localhost:8090',
        changeOrigin: true
      }
    }
  }
})
