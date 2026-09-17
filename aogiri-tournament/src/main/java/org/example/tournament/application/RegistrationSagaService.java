package org.example.tournament.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.tournament.infrastructure.RegistrationRepository;
import org.example.tournament.infrastructure.TournamentRegistrationEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationSagaService {

    private final RegistrationRepository repository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public RegistrationSagaService(RegistrationRepository repository, RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public TournamentRegistrationEntity initiateRegistration(Long tournamentId, Long playerId) {
        TournamentRegistrationEntity registration = new TournamentRegistrationEntity(tournamentId, playerId);
        TournamentRegistrationEntity saved = repository.save(registration);

        try {
            ObjectNode message = objectMapper.createObjectNode();
            message.put("registrationId", saved.getId());
            message.put("playerId", playerId);

            rabbitTemplate.convertAndSend("check_player_queue", message.toString());
            System.out.println("🟡 [Saga-Турніри] Початок: Заявка #" + saved.getId() + " збережена як PENDING. Запит відправлено.");

        } catch (Exception e) {
            System.err.println("Помилка відправки в RabbitMQ: " + e.getMessage());
        }

        return saved;
    }
}