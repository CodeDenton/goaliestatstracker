package com.creasevision.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class SchemaMigrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createsHistoricalDataTablesFromMigrations() {
        Integer tableCount = jdbcTemplate.queryForObject("""
                select count(*)
                from information_schema.tables
                where table_schema = 'PUBLIC'
                  and table_name in ('SEASONS', 'TEAMS', 'GOALIE_TEAM_STINTS',
                                     'GOALIE_SEASON_STATS', 'GAMES', 'GOALIE_GAME_STATS',
                                     'SHOT_ZONE_AGGREGATES', 'IMPORT_RUNS', 'MODEL_VERSIONS')
                """, Integer.class);

        assertThat(tableCount).isEqualTo(9);
    }
}
