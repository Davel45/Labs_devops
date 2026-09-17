package org.example.match.application;

import org.example.match.infrastructure.MatchEntity;
import org.example.match.infrastructure.MatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @InjectMocks
    private MatchService matchService;

    private MatchEntity matchEntity;
    private MatchCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        matchEntity = new MatchEntity("Team A vs Team B", "2026-10-15 19:00");
        matchEntity.setId(1L);
        createDTO = new MatchCreateDTO("Team A vs Team B", "2026-10-15 19:00");
    }

    @Test
    @DisplayName("create match saves entity and returns MatchDTO")
    void testCreateMatch() {
        when(matchRepository.save(any(MatchEntity.class))).thenReturn(matchEntity);

        MatchDTO result = matchService.create(createDTO);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.teams()).isEqualTo("Team A vs Team B");
        assertThat(result.matchDate()).isEqualTo("2026-10-15 19:00");

        verify(matchRepository).save(any(MatchEntity.class));
    }

    @Test
    @DisplayName("getById returns MatchDTO when match exists")
    void testGetById_Found() {
        when(matchRepository.findById(1L)).thenReturn(Optional.of(matchEntity));

        MatchDTO result = matchService.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.teams()).isEqualTo("Team A vs Team B");
    }

    @Test
    @DisplayName("getById returns null when match is not found")
    void testGetById_NotFound() {
        when(matchRepository.findById(99L)).thenReturn(Optional.empty());

        MatchDTO result = matchService.getById(99L);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getAll returns list of all matches")
    void testGetAllMatches() {
        MatchEntity secondMatch = new MatchEntity("Team C vs Team D", "2026-10-16 20:00");
        secondMatch.setId(2L);
        when(matchRepository.findAll()).thenReturn(List.of(matchEntity, secondMatch));

        List<MatchDTO> results = matchService.getAll();

        assertThat(results).hasSize(2);
        assertThat(results.get(0).teams()).isEqualTo("Team A vs Team B");
        assertThat(results.get(1).teams()).isEqualTo("Team C vs Team D");
    }

    @Test
    @DisplayName("update match modifies entity and returns updated MatchDTO")
    void testUpdateMatch_Found() {
        when(matchRepository.findById(1L)).thenReturn(Optional.of(matchEntity));
        when(matchRepository.save(any(MatchEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        MatchCreateDTO updateDTO = new MatchCreateDTO("Team X vs Team Y", "2026-11-01 21:00");
        MatchDTO result = matchService.update(1L, updateDTO);

        assertThat(result).isNotNull();
        assertThat(result.teams()).isEqualTo("Team X vs Team Y");
        assertThat(result.matchDate()).isEqualTo("2026-11-01 21:00");
    }

    @Test
    @DisplayName("update returns null when match not found")
    void testUpdateMatch_NotFound() {
        when(matchRepository.findById(99L)).thenReturn(Optional.empty());

        MatchDTO result = matchService.update(99L, new MatchCreateDTO("Team X vs Team Y", "2026-11-01 21:00"));

        assertThat(result).isNull();
        verify(matchRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete invokes repository deleteById")
    void testDeleteMatch() {
        matchService.delete(1L);

        verify(matchRepository).deleteById(1L);
    }
}
