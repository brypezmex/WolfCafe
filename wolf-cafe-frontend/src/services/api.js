/*
 * Central definition of the backend API location.
 *
 * The default is the RELATIVE path '/api'. In production nginx serves the built
 * app and reverse-proxies '/api' to the backend container, so every request
 * goes to whatever host the browser already loaded the page from. That is what
 * makes the same image work on localhost, on a LAN IP, and through a Cloudflare
 * Tunnel hostname without rebuilding anything.
 *
 * Override it at build time with VITE_API_BASE_URL when the API lives on a
 * different origin, for example:
 *   VITE_API_BASE_URL=https://api.example.com/api npm run build
 *
 * During `npm run dev` the Vite dev server proxies '/api' to the backend (see
 * vite.config.js), so the relative default works there too.
 */
const rawBaseUrl = import.meta.env.VITE_API_BASE_URL || '/api'

/** Backend API base URL with any trailing slashes removed. */
export const API_BASE_URL = rawBaseUrl.replace(/\/+$/, '')

/**
 * Builds a full API URL from a path.
 *
 * @param {string} path path beginning with '/', e.g. '/recipes'
 * @returns {string} the full URL to call
 */
export const apiUrl = (path = '') => `${API_BASE_URL}${path}`
