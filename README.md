# CreaseVision

CreaseVision makes NHL goalie data easy to explore, compare, and understand.

## Repository layout

```text
apps/
  web/        Next.js frontend
  api/        Spring Boot API and PostgreSQL integration
docs/         Architecture and product documentation
```

Database schema migrations live in `apps/api/src/main/resources/db/migration/` and are applied by Flyway on API startup.

## Local development

Start PostgreSQL and the API with Docker:

```bash
docker compose up --build
```

Then start the web app in another terminal:

```bash
cd apps/web
npm install
npm run dev
```

The frontend expects `NEXT_PUBLIC_API_URL` to point to the API, for example `http://localhost:8080` during local development.

## API tests

The API uses JUnit 5, Mockito, and an in-memory H2 database for tests. The test configuration never connects to Supabase or calls the NHL API.

```bash
cd apps/api
./mvnw test
```

NHL refreshes run at 1:00 AM Vancouver time while the API is awake. The service also refreshes on startup; on Render Free, this is the dependable fallback because an in-process scheduler cannot wake a sleeping service. A later external scheduled trigger can call a protected refresh endpoint once that endpoint is introduced.
