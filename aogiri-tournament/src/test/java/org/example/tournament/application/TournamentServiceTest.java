package org.example.tournament.application;

import org.example.tournament.infrastructure.TournamentEntity;
import org.example.tournament.infrastructure.TournamentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TournamentServiceTest {

    @Mock
    private TournamentRepository repository;

    @Mock
    private RestTemplate restTemplate;

    private TournamentService tournamentService;

    private final String userServiceUrl = "http://localhost:8081";
    private final String matchServiceUrl = "http://localhost:8083";

    @BeforeEach
    void setUp() {
        tournamentService = new TournamentService(
                repository,
                restTemplate,
                userServiceUrl,
                matchServiceUrl
        );
    }

    @Test
    @DisplayName("create tournament saves entity and returns TournamentDTO")
    void testCreateTournament() {
        TournamentEntity entity = new TournamentEntity("Summer Cup 2026");
        entity.setId(1L);
        when(repository.save(any(TournamentEntity.class))).thenReturn(entity);

        TournamentDTO result = tournamentService.create(new TournamentCreateDTO("Summer Cup 2026"));

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Summer Cup 2026");
        verify(repository).save(any(TournamentEntity.class));
    }

    @Test
    @DisplayName("getById returns TournamentDTO when tournament exists")
    void testGetById_Found() {
        TournamentEntity entity = new TournamentEntity("Summer Cup 2026");
        entity.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        TournamentDTO result = tournamentService.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Summer Cup 2026");
    }

    @Test
    @DisplayName("getById returns null when tournament is missing")
    void testGetById_NotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        TournamentDTO result = tournamentService.getById(99L);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getAll returns list of all tournaments")
    void testGetAll() {
        TournamentEntity t1 = new TournamentEntity("Tournament 1");
        t1.setId(1L);
        TournamentEntity t2 = new TournamentEntity("Tournament 2");
        t2.setId(2L);
        when(repository.findAll()).thenReturn(List.of(t1, t2));

        List<TournamentDTO> results = tournamentService.getAll();

        assertThat(results).hasSize(2);
        assertThat(results.get(0).name()).isEqualTo("Tournament 1");
        assertThat(results.get(1).name()).isEqualTo("Tournament 2");
    }

    @Test
    @DisplayName("update tournament updates name when found")
    void testUpdate_Found() {
        TournamentEntity entity = new TournamentEntity("Old Name");
        entity.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(any(TournamentEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        TournamentDTO result = tournamentService.update(1L, new TournamentCreateDTO("New Name"));

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("New Name");
    }

    @Test
    @DisplayName("update tournament returns null when not found")
    void testUpdate_NotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        TournamentDTO result = tournamentService.update(99L, new TournamentCreateDTO("New Name"));

        assertThat(result).isNull();
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("delete removes tournament by id")
    void testDelete() {
        tournamentService.delete(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("getTopPlayer queries external user service via RestTemplate")
    void testGetTopPlayer_Success() {
        PlayerResponseDTO mockPlayer = new PlayerResponseDTO(10L, "CyberChampion");
        when(restTemplate.getForObject(userServiceUrl + "/api/v1/players/10", PlayerResponseDTO.class))
                .thenReturn(mockPlayer);

        PlayerResponseDTO player = tournamentService.getTopPlayer(10L);

        assertThat(player).isNotNull();
        assertThat(player.id()).isEqualTo(10L);
        assertThat(player.nickname()).isEqualTo("CyberChampion");
    }

    @Test
    @DisplayName("playerFallback returns fallback DTO with error message")
    void testPlayerFallback() {
        PlayerResponseDTO fallback = tournamentService.playerFallback(10L, new RuntimeException("Service down"));

        assertThat(fallback).isNotNull();
        assertThat(fallback.id()).isEqualTo(10L);
        assertThat(fallback.nickname()).isEqualTo("[Гравець тимчасово недоступний]");
    }

    @Test
    @DisplayName("getFinalMatch queries external match service via RestTemplate")
    void testGetFinalMatch_Success() {
        MatchResponseDTO mockMatch = new MatchResponseDTO(5L, "Team A vs Team B", "2026-10-01 18:00");
        when(restTemplate.getForObject(matchServiceUrl + "/api/v1/matches/5", MatchResponseDTO.class))
                .thenReturn(mockMatch);

        MatchResponseDTO match = tournamentService.getFinalMatch(5L);

        assertThat(match).isNotNull();
        assertThat(match.id()).isEqualTo(5L);
        assertThat(match.teams()).isEqualTo("Team A vs Team B");
        assertThat(match.matchDate()).isEqualTo("2026-10-01 18:00");
    }

    @Test
    @DisplayName("matchFallback returns fallback DTO when match service fails")
    void testMatchFallback() {
        MatchResponseDTO fallback = tournamentService.matchFallback(5L, new RuntimeException("Timeout"));

        assertThat(fallback).isNotNull();
        assertThat(fallback.id()).isEqualTo(5L);
        assertThat(fallback.teams()).isEqualTo("[Матч не визначено]");
        assertThat(fallback.matchDate()).isEqualTo("[Дата невідома]");
    }
}
