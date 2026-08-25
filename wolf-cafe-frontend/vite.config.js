import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')

  // Where `npm run dev` forwards /api requests. In production nginx does this
  // instead, so application code always uses the relative '/api' path and never
  // needs to know the deployed hostname.
  const devApiTarget = env.DEV_API_PROXY_TARGET || 'http://localhost:8080'

  return {
    plugins: [react()],
    server: {
      port: Number(env.PORT) || 3000,
      // Listen on all interfaces so the dev server is reachable from a VM or
      // another device on the network, not just 127.0.0.1.
      host: true,
      proxy: {
        '/api': {
          target: devApiTarget,
          changeOrigin: true
        }
      }
    },
    preview: {
      port: Number(env.PORT) || 3000,
      host: true
    }
  }
})
