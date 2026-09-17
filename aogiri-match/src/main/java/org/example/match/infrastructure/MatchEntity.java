package org.example.match.infrastructure;

import jakarta.persistence.*;

@Entity
@Table(name = "matches")
public class MatchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String teams;
    private String matchDate;

    public MatchEntity() {}

    public MatchEntity(String teams, String matchDate) {
        this.teams = teams;
        this.matchDate = matchDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTeams() { return teams; }
    public void setTeams(String teams) { this.teams = teams; }
    public String getMatchDate() { return matchDate; }
    public void setMatchDate(String matchDate) { this.matchDate = matchDate; }
}