# Analytics API

All endpoints return JSON. Invalid request parameters return a consistent `400` response with `code`, `message`, and `timestamp`; absent resources return `404`.

| Endpoint | Purpose |
| --- | --- |
| `GET /api/leaderboard?season=20252026` | Season leaderboard; supports `team`, `minimumGames`, `sort`, `direction`, `page`, `size`, and `search`. |
| `GET /api/goalies/{goalieId}/profile` | Goalie profile and historical season lines. |
| `GET /api/goalies/{goalieId}/games?page=0&size=25` | Newest-first game log, including opponent, score, decision, and goalie stats. |
| `GET /api/goalies/{goalieId}/shot-map?season=20252026[&gameId=...]` | Actual NHL shot coordinates and zone summaries. |
| `GET /api/games?season=20252026[&team=PIT&page=0&size=25]` | Newest-first regular-season game index. |
| `GET /api/games/{gameId}` | Game metadata, every goalie stat line, and each goalie's shot map. |
| `GET /api/goalies/compare?season=20252026&goalieId=1&goalieId=2` | Aggregated regular-season comparison for two to four goalies. |

To backfill map coverage, start the API once with `NHL_IMPORT_GAME_SEASON=20252026`. This imports every regular-season game discovered from the NHL club schedules; repeated runs upsert the same games, goalie lines, and shot events.

The corresponding OpenAPI description is in [`openapi.yaml`](openapi.yaml).
