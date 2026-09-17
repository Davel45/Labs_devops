package org.example.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, Long> {
    // Метод, який знайде всі записи, де processed = false
    List<OutboxEventEntity> findByProcessedFalse();
}