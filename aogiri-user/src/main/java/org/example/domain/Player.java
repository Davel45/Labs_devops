package org.example.domain;

public class Player {
    private Long id;
    private String nickname;

    public Player(Long id, String nickname) { this.id = id; this.nickname = nickname; }
    public Long getId() { return id; }
    public String getNickname() { return nickname; }
}