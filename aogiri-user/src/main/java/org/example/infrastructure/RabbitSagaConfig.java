package org.example.infrastructure; // зміни на свій пакет, якщо треба

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitSagaConfig {

    // Автоматично створюємо чергу для ЗАПИТІВ
    @Bean
    public Queue checkPlayerQueue() {
        return new Queue("check_player_queue", true); // true означає, що черга переживе рестарт RabbitMQ
    }

    // Автоматично створюємо чергу для ВІДПОВІДЕЙ
    @Bean
    public Queue playerCheckedQueue() {
        return new Queue("player_checked_queue", true);
    }
}