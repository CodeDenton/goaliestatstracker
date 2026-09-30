# Database migrations

This is the Flyway migration directory. Every production schema change is an ordered SQL file committed here, for example `V3__add_goalie_contracts.sql`.

`V1__legacy_hibernate_schema.sql` recreates the tables that Hibernate previously generated. `V2__historical_goalie_data_foundation.sql` adds the season-aware data model.

The current Supabase database already contains the legacy Hibernate tables. On its first Flyway deployment, the application baselines that existing schema at version 1 and applies version 2; it does not recreate or erase the legacy tables. Fresh databases run both migrations from scratch.

Do not edit a migration that has already reached a shared database. Add the next versioned migration instead.
