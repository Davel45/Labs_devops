package org.example.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.application.PlayerCreateDTO;
import org.example.application.PlayerDTO;
import org.example.application.PlayerService;
import org.example.application.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlayerController.class)
class PlayerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlayerService playerService;

    @MockBean
    private TokenService tokenService;

    @Test
    @DisplayName("POST /api/v1/players creates player and returns 200")
    void testCreatePlayer_ValidRequest_ReturnsOk() throws Exception {
        PlayerCreateDTO request = new PlayerCreateDTO("ProGamer");
        PlayerDTO response = new PlayerDTO(1L, "ProGamer");

        when(playerService.createPlayer(any(PlayerCreateDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nickname").value("ProGamer"));
    }

    @Test
    @DisplayName("POST /api/v1/players returns 400 when nickname is blank or too short")
    void testCreatePlayer_InvalidRequest_ReturnsBadRequest() throws Exception {
        PlayerCreateDTO request = new PlayerCreateDTO("");

        mockMvc.perform(post("/api/v1/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/players/{id} returns player when found")
    void testGetPlayerById_ReturnsOk() throws Exception {
        PlayerDTO response = new PlayerDTO(1L, "ProGamer");
        when(playerService.getPlayerById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/players/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nickname").value("ProGamer"));
    }

    @Test
    @DisplayName("GET /api/v1/players returns list of all players")
    void testGetAllPlayers_ReturnsList() throws Exception {
        List<PlayerDTO> players = List.of(
                new PlayerDTO(1L, "ProGamer"),
                new PlayerDTO(2L, "ShadowNinja")
        );
        when(playerService.getAllPlayers()).thenReturn(players);

        mockMvc.perform(get("/api/v1/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nickname").value("ProGamer"))
                .andExpect(jsonPath("$[1].nickname").value("ShadowNinja"));
    }

    @Test
    @DisplayName("DELETE /api/v1/players/{id} deletes player and returns 200")
    void testDeletePlayer_ReturnsOk() throws Exception {
        mockMvc.perform(delete("/api/v1/players/1"))
                .andExpect(status().isOk());
    }
}
