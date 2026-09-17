package org.example.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PlayerCheckSagaListener {

    private final PlayerRepository playerRepository; // Твій репозиторій для роботи з БД
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public PlayerCheckSagaListener(PlayerRepository playerRepository,
                                   RabbitTemplate rabbitTemplate,
                                   ObjectMapper objectMapper) {
        this.playerRepository = playerRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "check_player_queue")
    public void handleCheckPlayerRequest(String message) {
        try {
            JsonNode requestNode = objectMapper.readTree(message);
            Long registrationId = requestNode.get("registrationId").asLong();
            Long playerId = requestNode.get("playerId").asLong();

            System.out.println("🟡 [Saga-Гравці] Отримано запит на перевірку гравця ID=" + playerId + " (Заявка #" + registrationId + ")");

            boolean exists = playerRepository.existsById(playerId);

            ObjectNode replyNode = objectMapper.createObjectNode();
            replyNode.put("registrationId", registrationId);
            replyNode.put("playerExists", exists);

            rabbitTemplate.convertAndSend("player_checked_queue", replyNode.toString());

            System.out.println("🟢 [Saga-Гравці] Відповідь відправлено: Гравець знайдений? -> " + exists);

        } catch (Exception e) {
            System.err.println("❌ Помилка під час перевірки гравця: " + e.getMessage());
        }
    }
}