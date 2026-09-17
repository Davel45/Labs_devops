package org.example;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtUtil {

    private final String SECRET = "MySuperSecretKeyForAogiriEsportsProject2026!";
    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public void validateToken(final String token) {
        Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }
}