package org.example.tournament.infrastructure;

import jakarta.persistence.*;

@Entity
@Table(name = "tournaments") // Назва таблиці в базі
public class TournamentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    public TournamentEntity() {}

    public TournamentEntity(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}