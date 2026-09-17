package org.example.tournament.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tournament.domain.RegistrationStatus;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SagaReplyListener {

    private final RegistrationRepository repository;
    private final ObjectMapper objectMapper;

    public SagaReplyListener(RegistrationRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "player_checked_queue")
    public void handleReply(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            Long registrationId = jsonNode.get("registrationId").asLong();
            boolean playerExists = jsonNode.get("playerExists").asBoolean();

            TournamentRegistrationEntity registration = repository.findById(registrationId)
                    .orElseThrow(() -> new RuntimeException("Заявку не знайдено"));

            if (playerExists) {
                registration.setStatus(RegistrationStatus.CONFIRMED);
                repository.save(registration);
                System.out.println("🟢 [Saga-Турніри] Успіх: Гравець підтверджений. Заявка #" + registrationId + " -> CONFIRMED.");
            } else {
                // ВІДКАТ (КОМПЕНСАЦІЯ)
                registration.setStatus(RegistrationStatus.CANCELLED);
                repository.save(registration);
                System.out.println("🔴 [Saga-Турніри] ВІДКАТ: Гравця немає в базі! Заявка #" + registrationId + " скасована -> CANCELLED.");
            }

        } catch (Exception e) {
            System.err.println("Помилка обробки відповіді: " + e.getMessage());
        }
    }
}