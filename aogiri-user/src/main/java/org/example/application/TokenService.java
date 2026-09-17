package org.example.application;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Service
public class TokenService {

    private final String SECRET = "MySuperSecretKeyForAogiriEsportsProject2026!";
    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public String generateToken(Long userId, String nickname) {
        Map<String, Object> claims = Map.of("nickname", nickname);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId.toString())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // діє 1 годину
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}