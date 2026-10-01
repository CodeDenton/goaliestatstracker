package com.creasevision.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.creasevision.api.dto.ShotMapDTO;
import com.creasevision.api.model.ShotEvent;
import com.creasevision.api.repository.ShotEventRepository;

@ExtendWith(MockitoExtension.class)
class ShotMapServiceTest {
    @Mock
    private ShotEventRepository events;

    @Test
    void returnsRealShotCoordinatesAndSampledSavePercentageZones() {
        when(events.findByGoalieIdAndSeasonIdOrderByGameIdAscNhlEventIdAsc(31L, "20252026"))
                .thenReturn(List.of(event(31, 1L, 80, 4, "SAVE"), event(32, 1L, 76, 8, "GOAL")));

        ShotMapDTO map = new ShotMapService(events).goalieMap(31L, "20252026", null);

        assertThat(map.shots()).extracting(ShotMapDTO.ShotDTO::eventId, ShotMapDTO.ShotDTO::outcome)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(31, "SAVE"), org.assertj.core.groups.Tuple.tuple(32, "GOAL"));
        assertThat(map.zones()).filteredOn(zone -> zone.code().equals("INNER_SLOT"))
                .singleElement().extracting(ShotMapDTO.ZoneDTO::shots, ShotMapDTO.ZoneDTO::saves,
                        ShotMapDTO.ZoneDTO::goals, ShotMapDTO.ZoneDTO::savePctg)
                .containsExactly(2, 1, 1, .5d);
    }

    private ShotEvent event(int id, long gameId, int x, int y, String outcome) {
        ShotEvent event = new ShotEvent();
        event.setGameId(gameId);
        event.setNhlEventId(id);
        event.setGoalieId(31L);
        event.setXCoordinate(x);
        event.setYCoordinate(y);
        event.setOutcome(outcome);
        return event;
    }
}
