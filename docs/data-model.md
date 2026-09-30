# Historical goalie data model

This schema separates a goalie's stable identity from data that changes by season, team, or game. That makes it possible to support historical leaderboards, traded goalies, game logs, and future shot-zone analytics without overwriting prior seasons.

```text
goalies ──< goalie_team_stints >── teams
   │                 │
   │                 └──< goalie_season_stats
   │
   ├──< goalie_game_stats >── games >── seasons
   └──< shot_zone_aggregates >── seasons / optional games

data_sources ──< import_runs
model_versions
```

## Core tables

- `goalies` stores the NHL player ID and stable biography: name, height, weight, catching hand, birth information, nationality, position, and headshot.
- `seasons` stores NHL season IDs such as `20262027` and their human-readable labels.
- `teams` stores team identity and branding separately from player data.
- `goalie_team_stints` represents a goalie on a team in a season. A goalie can have multiple stints, which supports trades.
- `goalie_season_stats` stores one season-stat line per stint.
- `games` and `goalie_game_stats` support future game logs and per-game goalie views.
- `shot_zone_aggregates` supports both season and game shot-zone summaries, without committing to raw event storage before the source is validated.
- `data_sources`, `import_runs`, and `model_versions` make imports and calculated metrics attributable and observable.

## Migration and deployment behavior

Flyway owns the production schema. Hibernate runs with `ddl-auto=validate`, which verifies that entity mappings agree with the migrated schema but never creates, alters, or drops production tables.

The first Flyway migration records the legacy Hibernate schema. For the existing Supabase database, Flyway baselines at version 1 and then applies version 2, retaining the current 2026-27 snapshot. Fresh databases apply both migrations normally.

The current NHL importer still writes the legacy current-season snapshot, and now also fills the stable goalie biography fields from NHL roster data. The ingestion follow-up will write `goalie_team_stints` and `goalie_season_stats` directly, then backfill prior seasons. Until then, the version-2 tables are a safe foundation rather than the API's active read model.
