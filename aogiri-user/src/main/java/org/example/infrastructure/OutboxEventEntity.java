package org.example.infrastructure;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_events")
public class OutboxEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventType; // Наприклад: "PLAYER_CREATED"

    @Column(columnDefinition = "TEXT")
    private String payload; // Тут буде JSON з даними гравця

    private LocalDateTime createdAt;

    private boolean processed; // Статус: відправлено в RabbitMQ чи ще ні?

    public OutboxEventEntity() {}

    public OutboxEventEntity(String eventType, String payload) {
        this.eventType = eventType;
        this.payload = payload;
        this.createdAt = LocalDateTime.now();
        this.processed = false; // При створенні подія ще не оброблена
    }

    // Геттери та Сеттери
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public boolean isProcessed() { return processed; }
    public void setProcessed(boolean processed) { this.processed = processed; }
}