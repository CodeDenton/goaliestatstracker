package com.creasevision.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.creasevision.api.service.LeaderboardService;

@ExtendWith(MockitoExtension.class)
class LeaderboardControllerTest {
    @Mock private LeaderboardService leaderboard;

    @Test
    void forwardsShareableLeaderboardParametersToTheService() {
        LeaderboardService.LeaderboardPage expected = new LeaderboardService.LeaderboardPage("20252026", 1, 25, 26, List.of());
        when(leaderboard.leaderboard("20252026", "PIT", 10, "wins", "asc", 1, 25, "jarry")).thenReturn(expected);

        var response = new LeaderboardController(leaderboard)
                .leaderboard("20252026", "PIT", 10, "wins", "asc", 1, 25, "jarry");

        assertThat(response).isSameAs(expected);
        verify(leaderboard).leaderboard("20252026", "PIT", 10, "wins", "asc", 1, 25, "jarry");
    }
}
