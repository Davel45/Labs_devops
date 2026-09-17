package org.example.tournament.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface RegistrationRepository extends JpaRepository<TournamentRegistrationEntity, Long> {

    List<TournamentRegistrationEntity> findByPlayerId(Long playerId);
}
