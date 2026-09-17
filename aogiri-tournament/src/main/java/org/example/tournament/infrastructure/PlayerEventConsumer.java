package org.example.tournament.infrastructure;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PlayerEventConsumer {

    @RabbitListener(queues = "player_events_queue")
    public void handlePlayerCreatedEvent(String message) {
        System.out.println("[Consumer] Мікросервіс Турнірів асинхронно отримав дані нового гравця: " + message);

    }
}