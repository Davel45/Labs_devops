package org.example.tournament.domain;

public class Tournament {
    private Long id;
    private String name;

    public Tournament(Long id, String name) { this.id = id; this.name = name; }
    public Long getId() { return id; }
    public String getName() { return name; }
}