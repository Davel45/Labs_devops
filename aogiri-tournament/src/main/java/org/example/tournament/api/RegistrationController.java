package org.example.tournament.api;

import org.example.tournament.application.RegistrationSagaService;
import org.example.tournament.infrastructure.RegistrationRepository;
import org.example.tournament.infrastructure.TournamentRegistrationEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/registrations")
public class RegistrationController {

    private final RegistrationSagaService sagaService;
    private final RegistrationRepository repository;

    public RegistrationController(RegistrationSagaService sagaService, RegistrationRepository repository) {
        this.sagaService = sagaService;
        this.repository = repository;
    }

    @PostMapping("/tournament/{tournamentId}/player/{playerId}")
    public TournamentRegistrationEntity registerPlayer(
            @PathVariable Long tournamentId,
            @PathVariable Long playerId) {


        return sagaService.initiateRegistration(tournamentId, playerId);
    }

    @GetMapping("/player/{playerId}")
    public List<TournamentRegistrationEntity> getPlayerRegistrations(@PathVariable Long playerId) {
        return repository.findByPlayerId(playerId);
    }
}