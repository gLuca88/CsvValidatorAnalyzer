package com.gianluca.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

	private static final long EXPIRATION_TIME_MS = 1000 * 60 * 60 * 5; // 5 ore
	private static final String SECRET_KEY = "mysecretkeyformyjwtgianlucatestsecure123456789"; // al meno 32 byte

	private Key getSigningKey() {
		return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
	}

	public String generateToken(String username, String role) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("role", role);

		return Jwts.builder().setSubject(username).addClaims(claims).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
				.signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
	}

	public String extractUsername(String token) {
		return getClaims(token).getSubject();
	}

	public String extractRole(String token) {
		return (String) getClaims(token).get("role");
	}

	public boolean isTokenValid(String token) {
		try {
			getClaims(token);
			return true;
		} catch (JwtException e) {
			return false;
		}
	}

	private Claims getClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
	}
}
