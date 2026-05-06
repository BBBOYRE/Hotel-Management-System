import { defineConfig } from "vite";

export default defineConfig({
  build: {
    outDir: "../src/main/resources/static",
    emptyOutDir: true
  },
  server: {
    host: "0.0.0.0",
    port: 5173,
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true
      }
    }
  }
});
