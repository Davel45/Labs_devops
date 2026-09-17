package org.example.tournament.infrastructure;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitSagaConfig {

    @Bean
    public Queue checkPlayerQueue() {
        return new Queue("check_player_queue", true);
    }

    @Bean
    public Queue playerCheckedQueue() {
        return new Queue("player_checked_queue", true);
    }
}