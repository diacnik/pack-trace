# PackTrace

PackTrace is a gear tracking app for outdoor packs, letting users catalog items, organize by pack, and manage their loadout.

## Tech stack

- Backend: Quarkus (Java), REST, JDBC
- Database: PostgreSQL
- Auth: Auth0 (OIDC/JWT)
- Frontend: Svelte + Vite

## Local setup

### Prerequisites

- Java (for Gradle/Quarkus)
- Node.js and npm (for the frontend)
- PostgreSQL (local or remote)
- Docker (optional, for local Postgres via Compose)

### Environment variables (backend)

The backend reads its configuration from environment variables. An example file is provided at `.env.example`.

Export the variables in your shell before running the backend:

```shell script
export DB_USER=your_db_user
export DB_PASSWORD=your_db_password
export DB_URL=jdbc:postgresql://localhost:5432/pack_trace
export AUTH0_DOMAIN=https://your-tenant.auth0.com
export AUTH0_AUDIENCE=your-api-audience
```

### Database via Docker Compose (optional)

If you prefer not to install Postgres locally, you can run it with Docker Compose:

```shell script
docker compose up -d
```

To stop and remove the container and wipe the data volume:

```shell script
docker compose down -v
```

This uses the defaults defined in `docker-compose.yml`. Set your env vars to match:

```shell script
export DB_USER=packtrace
export DB_PASSWORD=packtrace
export DB_URL=jdbc:postgresql://localhost:5432/pack_trace
```

### Run locally

1. Start the backend (Quarkus):

```shell script
./gradlew quarkusDev
```

2. Start the frontend (from `frontend/`):

```shell script
npm install
npm run dev
```

The frontend expects the backend at `http://localhost:8080` and proxies `/api/*` requests there.

## Quarkus

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.
