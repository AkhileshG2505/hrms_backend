package com.texlaculture.leave.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {


    private static final String SECRET = "texlaculture-hrms-leave-service-super-secret-key-change-me-1234567890";
    private static final long EXPIRATION_MS = 1000 * 60 * 60 * 8; // 8 hours

    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public String generateToken(UserDetails userDetails) {
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(Object::toString)
                .orElse("");

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            Date expiration = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();

            return username.equals(userDetails.getUsername()) && expiration.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}