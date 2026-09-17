package org.example.tournament.api;

import jakarta.validation.Valid;
import org.example.tournament.application.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tournaments")
public class TournamentController {

    private static final Logger log = LoggerFactory.getLogger(TournamentController.class);

    private final TournamentService service;

    public TournamentController(TournamentService service) {
        this.service = service;
    }

    @GetMapping
    public List<TournamentDTO> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public TournamentDTO getById(@PathVariable Long id) { return service.getById(id); }

    @GetMapping("/{tournamentId}/details/{playerId}/{matchId}")
    public String getTournamentDetails(@PathVariable Long tournamentId, @PathVariable Long playerId, @PathVariable Long matchId) {

        log.info("Отримано запит на деталі: Турнір ID={}, Гравець ID={}, Матч ID={}", tournamentId, playerId, matchId);

        TournamentDTO tournament = service.getById(tournamentId);

        PlayerResponseDTO topPlayer = service.getTopPlayer(playerId);
        MatchResponseDTO finalMatch = service.getFinalMatch(matchId);

        return String.format("Турнір: %s | Запрошена зірка: %s | Головний матч: %s (Відбудеться: %s)",
                tournament.name(), topPlayer.nickname(), finalMatch.teams(), finalMatch.matchDate());
    }

    @PostMapping
    public TournamentDTO create(@Valid @RequestBody TournamentCreateDTO dto) { return service.create(dto); }

    @PutMapping("/{id}")
    public TournamentDTO update(@PathVariable Long id, @Valid @RequestBody TournamentCreateDTO dto) { return service.update(id, dto); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { service.delete(id); }
}