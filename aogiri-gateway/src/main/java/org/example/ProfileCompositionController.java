package org.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/composition")
public class ProfileCompositionController {

    private final WebClient.Builder webClientBuilder;
    private final String userServiceUrl;
    private final String tournamentServiceUrl;

    public ProfileCompositionController(
            WebClient.Builder webClientBuilder,
            @Value("${services.user.url:http://127.0.0.1:8081}") String userServiceUrl,
            @Value("${services.tournament.url:http://127.0.0.1:8082}") String tournamentServiceUrl) {
        this.webClientBuilder = webClientBuilder;
        this.userServiceUrl = userServiceUrl;
        this.tournamentServiceUrl = tournamentServiceUrl;
    }

    @GetMapping("/player-profile/{playerId}")
    public Mono<Map<String, Object>> getPlayerProfile(@PathVariable Long playerId) {

        Mono<Object> playerMono = webClientBuilder.build().get()
                .uri(userServiceUrl + "/api/v1/players/" + playerId)
                .retrieve()
                .bodyToMono(Object.class)
                .onErrorResume(e -> Mono.just(Map.of("error", "Сервіс гравців недоступний")));

        Mono<Object> tournamentsMono = webClientBuilder.build().get()
                .uri(tournamentServiceUrl + "/api/v1/registrations/player/" + playerId)
                .retrieve()
                .bodyToMono(Object.class)
                .onErrorResume(e -> {
                    System.err.println("Сервіс турнірів лежить! Повертаємо пустий список турнірів.");
                    return Mono.just(List.of());
                });

        return Mono.zip(playerMono, tournamentsMono)
                .map(tuple -> Map.of(
                        "playerInfo", tuple.getT1(),
                        "tournamentRegistrations", tuple.getT2()
                ));
    }
}