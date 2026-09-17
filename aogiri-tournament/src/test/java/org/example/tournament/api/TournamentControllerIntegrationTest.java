package org.example.tournament.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tournament.application.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TournamentController.class)
class TournamentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TournamentService tournamentService;

    @Test
    @DisplayName("GET /api/v1/tournaments returns list of tournaments")
    void testGetAllTournaments() throws Exception {
        when(tournamentService.getAll()).thenReturn(List.of(
                new TournamentDTO(1L, "Winter Cup"),
                new TournamentDTO(2L, "Spring Masters")
        ));

        mockMvc.perform(get("/api/v1/tournaments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Winter Cup"))
                .andExpect(jsonPath("$[1].name").value("Spring Masters"));
    }

    @Test
    @DisplayName("GET /api/v1/tournaments/{id} returns single tournament")
    void testGetTournamentById() throws Exception {
        when(tournamentService.getById(1L)).thenReturn(new TournamentDTO(1L, "Winter Cup"));

        mockMvc.perform(get("/api/v1/tournaments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Winter Cup"));
    }

    @Test
    @DisplayName("POST /api/v1/tournaments creates a tournament")
    void testCreateTournament() throws Exception {
        TournamentCreateDTO request = new TournamentCreateDTO("Championship 2026");
        TournamentDTO response = new TournamentDTO(1L, "Championship 2026");
        when(tournamentService.create(any(TournamentCreateDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/tournaments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Championship 2026"));
    }

    @Test
    @DisplayName("POST /api/v1/tournaments returns 400 for empty name")
    void testCreateTournament_InvalidName() throws Exception {
        TournamentCreateDTO request = new TournamentCreateDTO("");

        mockMvc.perform(post("/api/v1/tournaments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/tournaments/{tournamentId}/details/{playerId}/{matchId} returns formatted string")
    void testGetTournamentDetails() throws Exception {
        when(tournamentService.getById(1L)).thenReturn(new TournamentDTO(1L, "Major 2026"));
        when(tournamentService.getTopPlayer(2L)).thenReturn(new PlayerResponseDTO(2L, "S1mple"));
        when(tournamentService.getFinalMatch(3L)).thenReturn(new MatchResponseDTO(3L, "NaVi vs FaZe", "2026-11-20 19:00"));

        mockMvc.perform(get("/api/v1/tournaments/1/details/2/3"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Турнір: Major 2026")))
                .andExpect(content().string(containsString("Запрошена зірка: S1mple")))
                .andExpect(content().string(containsString("Головний матч: NaVi vs FaZe")));
    }

    @Test
    @DisplayName("DELETE /api/v1/tournaments/{id} deletes tournament")
    void testDeleteTournament() throws Exception {
        mockMvc.perform(delete("/api/v1/tournaments/1"))
                .andExpect(status().isOk());

        verify(tournamentService).delete(1L);
    }
}
