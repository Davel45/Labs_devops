package org.example.tournament.infrastructure;

import jakarta.persistence.*;
import org.example.tournament.domain.RegistrationStatus;

@Entity
@Table(name = "tournament_registrations")
public class TournamentRegistrationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tournamentId; // На який турнір реєструємось
    private Long playerId;     // Якого гравця реєструємо

    @Enumerated(EnumType.STRING)
    private RegistrationStatus status; // Наш статус (PENDING, CONFIRMED, CANCELLED)

    public TournamentRegistrationEntity() {}

    public TournamentRegistrationEntity(Long tournamentId, Long playerId) {
        this.tournamentId = tournamentId;
        this.playerId = playerId;
        this.status = RegistrationStatus.PENDING; // На старті статус ЗАВЖДИ в очікуванні
    }

    // Геттери та сеттери
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTournamentId() { return tournamentId; }
    public void setTournamentId(Long tournamentId) { this.tournamentId = tournamentId; }
    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }
    public RegistrationStatus getStatus() { return status; }
    public void setStatus(RegistrationStatus status) { this.status = status; }
}