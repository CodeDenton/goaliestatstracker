package com.creasevision.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.creasevision.api.model.Game;
import com.creasevision.api.model.Goalie;
import com.creasevision.api.model.GoalieGameStats;
import com.creasevision.api.model.Season;
import com.creasevision.api.model.ShotEvent;
import com.creasevision.api.model.Team;
import com.creasevision.api.repository.GameRepository;
import com.creasevision.api.repository.GoalieGameStatsRepository;
import com.creasevision.api.repository.GoalieRepository;
import com.creasevision.api.repository.SeasonRepository;
import com.creasevision.api.repository.ShotEventRepository;
import com.creasevision.api.repository.TeamRepository;

@SpringBootTest
@Transactional
class AnalyticsApiIntegrationTest {
    @Autowired WebApplicationContext context;
    @Autowired SeasonRepository seasons;
    @Autowired TeamRepository teams;
    @Autowired GoalieRepository goalies;
    @Autowired GameRepository games;
    @Autowired GoalieGameStatsRepository goalieStats;
    @Autowired ShotEventRepository shots;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        Season season = new Season(); season.setId("20252026"); season.setDisplayName("2025-26"); season.setStartsOn(LocalDate.of(2025, 10, 1)); season.setEndsOn(LocalDate.of(2026, 6, 30)); seasons.save(season);
        Team home = team("PIT"); Team away = team("BOS");
        Goalie goalie = new Goalie(); goalie.setId(31L); goalie.setFirstName("Test"); goalie.setLastName("Goalie"); goalies.save(goalie);
        Game game = new Game(); game.setId(2025020001L); game.setSeasonId("20252026"); game.setGameDate(LocalDate.of(2026, 4, 10)); game.setGameType((short) 2); game.setHomeTeamId(home.getId()); game.setAwayTeamId(away.getId()); game.setHomeScore(3); game.setAwayScore(2); game.setGameState("OFF"); game.setSourceName("test"); games.save(game);
        GoalieGameStats line = new GoalieGameStats(); line.setGameId(game.getId()); line.setGoalieId(goalie.getId()); line.setTeamId(home.getId()); line.setDecision("W"); line.setShotsAgainst(30); line.setSaves(28); line.setGoalsAgainst(2); line.setSavePctg(28d / 30); line.setSourceName("test"); goalieStats.save(line);
        ShotEvent shot = new ShotEvent(); shot.setGameId(game.getId()); shot.setSeasonId("20252026"); shot.setNhlEventId(1); shot.setGoalieId(goalie.getId()); shot.setDefendingTeamId(home.getId()); shot.setXCoordinate(80); shot.setYCoordinate(5); shot.setOutcome("SAVE"); shot.setSourceName("test"); shot.setSourceUpdatedAt(OffsetDateTime.now()); shots.save(shot);
    }

    @Test
    void servesEnrichedGameLogGameIndexAndFullGameMap() throws Exception {
        mvc.perform(get("/api/goalies/31/games"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.rows[0].opponent").value("BOS"))
                .andExpect(jsonPath("$.rows[0].teamScore").value(3));
        mvc.perform(get("/api/games").param("season", "20252026"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.rows[0].gameId").value(2025020001L));
        mvc.perform(get("/api/games/2025020001"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.goalieLines[0].shotMap.shots[0].outcome").value("SAVE"));
    }

    @Test
    void returnsStructuredBadRequestForInvalidPaging() throws Exception {
        mvc.perform(get("/api/goalies/31/games").param("size", "0"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("bad_request"));
    }

    private Team team(String abbreviation) { Team team = new Team(); team.setAbbreviation(abbreviation); team.setCommonName(abbreviation); return teams.save(team); }
}
