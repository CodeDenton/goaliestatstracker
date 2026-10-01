package com.creasevision.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.creasevision.api.service.GoalieProfileService;

@ExtendWith(MockitoExtension.class)
class GoalieProfileControllerTest {
    @Mock GoalieProfileService profiles;

    @Test void returnsOnlyTheRequestedGoalieProfile() {
        var expected = new GoalieProfileService.Profile(31L, "Tristan Jarry", List.of());
        when(profiles.profile(31L)).thenReturn(expected);
        assertThat(new GoalieProfileController(profiles).profile(31L)).isSameAs(expected);
        verify(profiles).profile(31L);
    }
}
