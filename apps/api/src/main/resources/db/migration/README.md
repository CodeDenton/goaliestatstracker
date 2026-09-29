# Database migrations

This is the standard Flyway migration directory. When Flyway is added, each schema change will be committed here as an ordered SQL file, for example `V1__create_core_hockey_schema.sql`.

The initial migration will replace Hibernate's development-only schema updates with a versioned, reproducible PostgreSQL schema.
