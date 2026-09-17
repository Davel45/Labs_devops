package org.example.tournament.application;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.example.tournament.infrastructure.TournamentEntity;
import org.example.tournament.infrastructure.TournamentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TournamentService {

    private final TournamentRepository repository;
    private final RestTemplate restTemplate;
    private final String userServiceUrl;
    private final String matchServiceUrl;

    public TournamentService(
            TournamentRepository repository,
            RestTemplate restTemplate,
            @Value("${services.user.url:http://localhost:8081}") String userServiceUrl,
            @Value("${services.match.url:http://localhost:8083}") String matchServiceUrl) {
        this.repository = repository;
        this.restTemplate = restTemplate;
        this.userServiceUrl = userServiceUrl;
        this.matchServiceUrl = matchServiceUrl;
    }

    private TournamentDTO convertToDto(TournamentEntity entity) {
        return new TournamentDTO(entity.getId(), entity.getName());
    }

    public List<TournamentDTO> getAll() { return repository.findAll().stream().map(this::convertToDto).collect(Collectors.toList()); }
    public TournamentDTO getById(Long id) { return repository.findById(id).map(this::convertToDto).orElse(null); }
    public TournamentDTO create(TournamentCreateDTO dto) { return convertToDto(repository.save(new TournamentEntity(dto.name()))); }
    public TournamentDTO update(Long id, TournamentCreateDTO dto) {
        return repository.findById(id).map(entity -> {
            entity.setName(dto.name());
            return convertToDto(repository.save(entity));
        }).orElse(null);
    }
    public void delete(Long id) { repository.deleteById(id); }


    @CircuitBreaker(name = "externalService", fallbackMethod = "playerFallback")
    @Retry(name = "externalService")
    public PlayerResponseDTO getTopPlayer(Long playerId) {
        String url = userServiceUrl + "/api/v1/players/" + playerId;
        return restTemplate.getForObject(url, PlayerResponseDTO.class);
    }

    public PlayerResponseDTO playerFallback(Long playerId, Exception e) {
        return new PlayerResponseDTO(playerId, "[Гравець тимчасово недоступний]");
    }

    @CircuitBreaker(name = "externalService", fallbackMethod = "matchFallback")
    @Retry(name = "externalService")
    public MatchResponseDTO getFinalMatch(Long matchId) {
        String url = matchServiceUrl + "/api/v1/matches/" + matchId;
        return restTemplate.getForObject(url, MatchResponseDTO.class);
    }

    public MatchResponseDTO matchFallback(Long matchId, Exception e) {
        return new MatchResponseDTO(matchId, "[Матч не визначено]", "[Дата невідома]");
    }
}