package org.example.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.infrastructure.OutboxEventEntity;
import org.example.infrastructure.OutboxEventRepository;
import org.example.infrastructure.PlayerEntity;
import org.example.infrastructure.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private OutboxEventRepository outboxRepository;

    private ObjectMapper objectMapper;
    private PlayerService playerService;

    private PlayerEntity playerEntity;
    private PlayerCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        playerService = new PlayerService(playerRepository, outboxRepository, objectMapper);
        playerEntity = new PlayerEntity("ProGamer");
        playerEntity.setId(1L);
        createDTO = new PlayerCreateDTO("ProGamer");
    }

    @Test
    @DisplayName("createPlayer saves player and persists outbox event")
    void testCreatePlayer_Success() throws Exception {
        when(playerRepository.save(any(PlayerEntity.class))).thenReturn(playerEntity);

        PlayerDTO result = playerService.createPlayer(createDTO);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.nickname()).isEqualTo("ProGamer");

        verify(playerRepository).save(any(PlayerEntity.class));

        ArgumentCaptor<OutboxEventEntity> captor = ArgumentCaptor.forClass(OutboxEventEntity.class);
        verify(outboxRepository).save(captor.capture());
        OutboxEventEntity capturedEvent = captor.getValue();
        assertThat(capturedEvent.getEventType()).isEqualTo("PLAYER_CREATED");
        assertThat(capturedEvent.getPayload()).contains("ProGamer");
    }

    @Test
    @DisplayName("createPlayer throws RuntimeException on repository failure")
    void testCreatePlayer_Failure_ThrowsException() {
        when(playerRepository.save(any(PlayerEntity.class)))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> playerService.createPlayer(createDTO));
        verify(outboxRepository, never()).save(any());
    }

    @Test
    @DisplayName("getPlayerById returns PlayerDTO when found")
    void testGetPlayerById_Found() {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(playerEntity));

        PlayerDTO result = playerService.getPlayerById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.nickname()).isEqualTo("ProGamer");
    }

    @Test
    @DisplayName("getPlayerById returns null when not found")
    void testGetPlayerById_NotFound() {
        when(playerRepository.findById(99L)).thenReturn(Optional.empty());

        PlayerDTO result = playerService.getPlayerById(99L);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getAllPlayers returns all player DTOs")
    void testGetAllPlayers() {
        PlayerEntity player2 = new PlayerEntity("ShadowNinja");
        player2.setId(2L);
        when(playerRepository.findAll()).thenReturn(List.of(playerEntity, player2));

        List<PlayerDTO> results = playerService.getAllPlayers();

        assertThat(results).hasSize(2);
        assertThat(results.get(0).nickname()).isEqualTo("ProGamer");
        assertThat(results.get(1).nickname()).isEqualTo("ShadowNinja");
    }

    @Test
    @DisplayName("updatePlayer updates nickname when player exists")
    void testUpdatePlayer_Success() {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(playerEntity));
        when(playerRepository.save(any(PlayerEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlayerCreateDTO updateDTO = new PlayerCreateDTO("UpdatedNickname");
        PlayerDTO result = playerService.updatePlayer(1L, updateDTO);

        assertThat(result).isNotNull();
        assertThat(result.nickname()).isEqualTo("UpdatedNickname");
    }

    @Test
    @DisplayName("updatePlayer returns null when player does not exist")
    void testUpdatePlayer_NotFound() {
        when(playerRepository.findById(99L)).thenReturn(Optional.empty());

        PlayerDTO result = playerService.updatePlayer(99L, new PlayerCreateDTO("NewNick"));

        assertThat(result).isNull();
        verify(playerRepository, never()).save(any());
    }

    @Test
    @DisplayName("deletePlayer invokes repository deleteById")
    void testDeletePlayer() {
        playerService.deletePlayer(1L);
        verify(playerRepository).deleteById(1L);
    }
}
