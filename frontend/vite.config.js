import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Dev proxy so relative /api calls hit Spring Boot (avoids CORS issues).
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
