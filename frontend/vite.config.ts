// @ts-nocheck
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react-swc";
import path from "path";
import { componentTagger } from "lovable-tagger";

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => ({
  server: {
    host: "::",
    port: 8080,
  },
  plugins: [
    react({
      jsxRuntime: 'automatic'
    }),
    mode === 'development' &&
    componentTagger(),
  ].filter(Boolean),
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  build: {
    target: 'esnext',
    rollupOptions: {
      onwarn() {
        return;
      }
    }
  },
  define: {
    global: 'globalThis',
  },
  esbuild: {
    target: 'esnext',
    loader: 'tsx',
    include: /\.(ts|tsx|js|jsx)$/,
    exclude: [],
    tsconfigRaw: `{
      "compilerOptions": {
        "jsx": "react-jsx",
        "allowJs": true,
        "skipLibCheck": true,
        "noEmit": true,
        "strict": false,
        "noUnusedLocals": false,
        "noUnusedParameters": false,
        "noImplicitAny": false,
        "noImplicitReturns": false,
        "noFallthroughCasesInSwitch": false,
        "exactOptionalPropertyTypes": false,
        "noUncheckedIndexedAccess": false,
        "suppressImplicitAnyIndexErrors": true
      }
    }`
  }
}));