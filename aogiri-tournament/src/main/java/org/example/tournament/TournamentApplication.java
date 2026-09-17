package org.example.tournament;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import java.time.Duration;

@SpringBootApplication
public class TournamentApplication {

    public static void main(String[] args) {
        SpringApplication.run(TournamentApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(org.springframework.beans.factory.ObjectProvider<RestTemplateBuilder> builderProvider) {
        RestTemplateBuilder builder = builderProvider.getIfAvailable();
        if (builder != null) {
            return builder
                    .setConnectTimeout(Duration.ofSeconds(2))
                    .setReadTimeout(Duration.ofSeconds(2))
                    .build();
        }
        return new RestTemplate();
    }
}