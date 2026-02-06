# PackTrace Frontend

Svelte + Vite frontend for PackTrace gear tracking application.

## Setup

1. Install dependencies:
```bash
npm install
```

2. Create a `.env` file with your Auth0 configuration (copy from `.env.example`):
```
VITE_AUTH0_DOMAIN=your-tenant.auth0.com
VITE_AUTH0_CLIENT_ID=your-client-id
VITE_AUTH0_AUDIENCE=your-api-audience
```

3. Start the development server:
```bash
npm run dev
```

The app will be available at http://localhost:3000

## Backend Proxy

The Vite dev server proxies `/api/*` requests to `http://localhost:8080` (Quarkus backend).
Make sure your backend is running before starting the frontend.

## Build

To build for production:
```bash
npm run build
```

The built files will be in the `dist` directory.
