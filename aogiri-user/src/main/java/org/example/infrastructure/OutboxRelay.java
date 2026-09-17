package org.example.infrastructure;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OutboxRelay {

    private final OutboxEventRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    public OutboxRelay(OutboxEventRepository outboxRepository, RabbitTemplate rabbitTemplate) {
        this.outboxRepository = outboxRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Bean
    public Queue playerEventsQueue() {
        return new Queue("player_events_queue", true);
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishOutboxEvents() {
        List<OutboxEventEntity> pendingEvents = outboxRepository.findByProcessedFalse();

        for (OutboxEventEntity event : pendingEvents) {
            try {
                rabbitTemplate.convertAndSend("player_events_queue", event.getPayload());

                event.setProcessed(true);
                outboxRepository.save(event);

                System.out.println("✅ [Relay] Успішно відправлено в RabbitMQ: " + event.getPayload());
            } catch (Exception e) {
                System.err.println("❌ [Relay] Помилка відправки події ID=" + event.getId());
            }
        }
    }
}