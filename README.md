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

To run a deliberate one-off season import locally, pass the NHL season ID when starting the API. This command is disabled unless the property is supplied:

```bash
cd apps/api
./mvnw spring-boot:run -Dspring-boot.run.arguments="--nhl.import-season=20232024"
```

For a resumable historical backfill, pass a comma-separated set of NHL season IDs. Each season receives its own import-run audit record; a failed season does not prevent the remaining requested seasons from running.

```bash
cd apps/api
./mvnw spring-boot:run -Dspring-boot.run.arguments="--nhl.import-seasons=20232024,20242025"
```

To validate a single NHL game boxscore before a broader game-log import:

```bash
cd apps/api
./mvnw spring-boot:run -Dspring-boot.run.arguments="--nhl.import-game-id=2025020001"
```

Historical imports use NHL regular-season goalie/team-split rows. Each row is upserted as one goalie-team stint, so traded goalies retain a separate statistical line for every team.
