package org.example.match.domain;

public class Match {
    private Long id;
    private String score;

    public Match(Long id, String score) { this.id = id; this.score = score; }
    public Long getId() { return id; }
    public String getScore() { return score; }
}