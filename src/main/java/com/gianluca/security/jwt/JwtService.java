package com.gianluca.security.jwt;


import java.util.Date;
import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private static final long EXPIRATION_TIME_MS = 1000 * 60 * 60 * 5; // 5 ore
    private static final String SECRET_KEY =
            "mysecretkeyformyjwtgianlucatestsecure123456789"; // almeno 32 byte

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // ------------------------------
    // GENERAZIONE TOKEN
    // ------------------------------
    public String generateToken(String username, String role) {

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
                .signWith(getSigningKey(), Jwts.SIG.HS256)   // <-- CORRETTO
                .compact();
    }

    // ------------------------------
    // ESTRAZIONE CLAIM
    // ------------------------------
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // ------------------------------
    // VALIDAZIONE TOKEN
    // ------------------------------
    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    // ------------------------------
    // PARSING CLAIM (nuova API)
    // ------------------------------
    private Claims getClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
