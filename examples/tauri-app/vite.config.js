import { defineConfig } from "vite";
import { svelte } from "@sveltejs/vite-plugin-svelte";

const host = process.env.TAURI_DEV_HOST;
const devPort = Number(process.env.TAURI_DEV_PORT || 1520);
const hmrPort = Number(process.env.TAURI_HMR_PORT || 1521);

// https://vite.dev/config/
export default defineConfig({
  plugins: [svelte()],

  // Vite options tailored for Tauri development and only applied in `tauri dev` or `tauri build`
  // prevent Vite from obscuring rust errors
  clearScreen: false,
  // tauri expects a fixed port, fail if that port is not available
  server: {
    host: host || false,
    port: devPort,
    strictPort: true,
    hmr: host ? {
      protocol: 'ws',
      host,
      port: hmrPort
    } : undefined,
  },
})
