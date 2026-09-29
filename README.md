# CreaseVision

CreaseVision makes NHL goalie data easy to explore, compare, and understand.

## Repository layout

```text
apps/
  web/        Next.js frontend
  api/        Spring Boot API and PostgreSQL integration
docs/         Architecture and product documentation
```

Database schema migrations belong in `apps/api/src/main/resources/db/migration/` and are applied by Flyway once it is introduced.

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
