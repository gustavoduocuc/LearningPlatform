package com.duoc.LearningPlatform.security;

import com.duoc.LearningPlatform.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtUtil(@Value("${jwt.secret:defaultSecretKeyForTestingOnly12345678901234567890}") String secret,
                   @Value("${jwt.expiration:86400000}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    // Protected no-args constructor for Spring proxy creation
    protected JwtUtil() {
        this.secretKey = Keys.hmacShaKeyFor("defaultSecretKeyForTestingOnly12345678901234567890".getBytes(StandardCharsets.UTF_8));
        this.expirationMs = 86400000;
    }

    public JwtUtil(String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = 86400000;
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public Long extractUserId(String token) {
        Claims claims = extractClaims(token);
        if (claims == null) {
            return null;
        }
        Object userId = claims.get("userId");
        return userId != null ? Long.valueOf(userId.toString()) : null;
    }

    public String extractEmail(String token) {
        Claims claims = extractClaims(token);
        if (claims == null) {
            return null;
        }
        return claims.get("email", String.class);
    }

    public String extractRole(String token) {
        Claims claims = extractClaims(token);
        if (claims == null) {
            return null;
        }
        return claims.get("role", String.class);
    }
}
