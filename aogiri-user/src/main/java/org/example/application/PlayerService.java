package org.example.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.infrastructure.OutboxEventEntity;
import org.example.infrastructure.OutboxEventRepository;
import org.example.infrastructure.PlayerEntity;
import org.example.infrastructure.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public PlayerService(PlayerRepository playerRepository,
                         OutboxEventRepository outboxRepository,
                         ObjectMapper objectMapper) {
        this.playerRepository = playerRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    // ТУТ ВИКОРИСТОВУЄМО ТВІЙ PlayerDTO
    private PlayerDTO convertToDto(PlayerEntity entity) {
        return new PlayerDTO(entity.getId(), entity.getNickname());
    }

    // Назви методів тепер збігаються з тим, що просить твій Контролер:
    public List<PlayerDTO> getAllPlayers() {
        return playerRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public PlayerDTO getPlayerById(Long id) {
        return playerRepository.findById(id).map(this::convertToDto).orElse(null);
    }

    @Transactional
    public PlayerDTO createPlayer(PlayerCreateDTO dto) {
        try {
            PlayerEntity savedPlayer = playerRepository.save(new PlayerEntity(dto.nickname()));

            PlayerDTO responseDto = convertToDto(savedPlayer);
            String jsonPayload = objectMapper.writeValueAsString(responseDto);

            OutboxEventEntity outboxEvent = new OutboxEventEntity("PLAYER_CREATED", jsonPayload);
            outboxRepository.save(outboxEvent);

            return responseDto;

        } catch (Exception e) {
            throw new RuntimeException("Помилка при збереженні гравця та події Outbox", e);
        }
    }

    public PlayerDTO updatePlayer(Long id, PlayerCreateDTO dto) {
        return playerRepository.findById(id).map(entity -> {
            entity.setNickname(dto.nickname());
            return convertToDto(playerRepository.save(entity));
        }).orElse(null);
    }

    public void deletePlayer(Long id) {
        playerRepository.deleteById(id);
    }
}