import { reactRouter } from "@react-router/dev/vite";
import tailwindcss from "@tailwindcss/vite";
import { defineConfig } from "vite";

export default defineConfig({
  plugins: [tailwindcss(), reactRouter()],
  resolve: {
    tsconfigPaths: true,
  },
  build: {
    commonjsOptions: {
      transformMixedEsModules: true,
      // Exclude MSW from CommonJS transformation to avoid resolver issues
      exclude: ["msw", "@mswjs/interceptors"],
    },
    rollupOptions: {
      // Mark MSW as external to prevent bundling issues
      external: (id) => {
        return id.includes("msw/node") || id.includes("@mswjs/interceptors");
      },
    },
  },
});
