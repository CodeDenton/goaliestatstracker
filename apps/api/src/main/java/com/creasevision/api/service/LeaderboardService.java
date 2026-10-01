package com.creasevision.api.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creasevision.api.model.Goalie;
import com.creasevision.api.model.GoalieSeasonStats;
import com.creasevision.api.model.GoalieTeamStint;
import com.creasevision.api.repository.GoalieRepository;
import com.creasevision.api.repository.GoalieSeasonStatsRepository;
import com.creasevision.api.repository.GoalieTeamStintRepository;
import com.creasevision.api.repository.TeamRepository;

@Service
public class LeaderboardService {
    private final GoalieRepository goalies;
    private final GoalieTeamStintRepository stints;
    private final GoalieSeasonStatsRepository stats;
    private final TeamRepository teams;

    public LeaderboardService(GoalieRepository goalies, GoalieTeamStintRepository stints,
            GoalieSeasonStatsRepository stats, TeamRepository teams) {
        this.goalies = goalies; this.stints = stints; this.stats = stats; this.teams = teams;
    }

    @Transactional(readOnly = true)
    public LeaderboardPage leaderboard(String season, String team, int minimumGames, String sort, String direction, int page, int size, String search) {
        if (page < 0 || size < 1 || size > 100 || minimumGames < 0) throw new IllegalArgumentException("Invalid leaderboard paging or minimum-games value.");
        List<GoalieTeamStint> seasonStints = stints.findBySeasonId(season);
        Map<Long, GoalieTeamStint> stintsById = seasonStints.stream().collect(Collectors.toMap(GoalieTeamStint::getId, stint -> stint));
        Map<Long, GoalieSeasonStats> statsByStint = stats.findByTeamStintIdIn(new ArrayList<>(stintsById.keySet())).stream().collect(Collectors.toMap(GoalieSeasonStats::getTeamStintId, line -> line));
        List<Long> goalieIds = seasonStints.stream().map(GoalieTeamStint::getGoalieId).distinct().sorted().toList();
        Map<Long, Goalie> goaliesById = goalies.findAllById(goalieIds).stream().collect(Collectors.toMap(Goalie::getId, goalie -> goalie));
        Map<Integer, String> teamNames = seasonStints.stream().map(GoalieTeamStint::getTeamId).distinct().collect(Collectors.toMap(id -> id, id -> teams.findById(id).map(value -> value.getAbbreviation()).orElse("UNK")));
        Map<Long, MutableRow> rows = new HashMap<>();
        for (GoalieTeamStint stint : seasonStints) {
            GoalieSeasonStats line = statsByStint.get(stint.getId()); if (line == null) continue;
            MutableRow row = rows.computeIfAbsent(stint.getGoalieId(), id -> new MutableRow(goaliesById.get(id)));
            row.add(line, teamNames.get(stint.getTeamId()));
        }
        List<Row> filtered = rows.values().stream().map(MutableRow::finish)
                .filter(row -> row.gamesPlayed() >= minimumGames)
                .filter(row -> team == null || team.isBlank() || row.teamAbbreviations().contains(team.toUpperCase(Locale.ROOT)))
                .filter(row -> search == null || search.isBlank() || row.name().toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT)))
                .sorted(comparator(sort, direction)).toList();
        int from = Math.min(page * size, filtered.size()); int to = Math.min(from + size, filtered.size());
        return new LeaderboardPage(season, page, size, filtered.size(), filtered.subList(from, to));
    }

    private Comparator<Row> comparator(String sort, String direction) {
        Comparator<Row> comparator = switch (sort == null ? "savePctg" : sort) {
            case "gamesPlayed" -> Comparator.comparingInt(Row::gamesPlayed);
            case "wins" -> Comparator.comparingInt(Row::wins);
            case "losses" -> Comparator.comparingInt(Row::losses);
            case "saves" -> Comparator.comparingInt(Row::saves);
            default -> Comparator.comparingDouble(Row::savePctg);
        };
        return "asc".equalsIgnoreCase(direction) ? comparator.thenComparing(Row::name) : comparator.reversed().thenComparing(Row::name);
    }

    public record LeaderboardPage(String season, int page, int size, int total, List<Row> rows) {}
    public record Row(Long goalieId, String name, List<String> teamAbbreviations, int gamesPlayed, int wins, int losses, int overtimeLosses, int saves, int shotsAgainst, double savePctg) {}
    private static final class MutableRow { final Goalie goalie; final List<String> teams = new ArrayList<>(); int games,wins,losses,ot,saves,shots;
        MutableRow(Goalie goalie) { this.goalie = goalie; }
        void add(GoalieSeasonStats line, String team) { teams.add(team); games+=zero(line.getGamesPlayed()); wins+=zero(line.getWins()); losses+=zero(line.getLosses()); ot+=zero(line.getOvertimeLosses()); saves+=zero(line.getSaves()); shots+=zero(line.getShotsAgainst()); }
        Row finish() { String name = goalie == null ? "Unknown goalie" : goalie.getFirstName()+" "+goalie.getLastName(); return new Row(goalie == null ? null : goalie.getId(), name.trim(), teams.stream().distinct().sorted().toList(),games,wins,losses,ot,saves,shots,shots==0?0:(double)saves/shots); }
        int zero(Integer value) { return value == null ? 0 : value; }
    }
}
