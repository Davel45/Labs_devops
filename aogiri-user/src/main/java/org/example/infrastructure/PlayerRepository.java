package org.example.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends JpaRepository<PlayerEntity, Long> {
    // Тіло залишається порожнім!
}