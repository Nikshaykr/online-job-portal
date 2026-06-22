package com.jobportal.security;

import com.jobportal.model.Role;
import com.jobportal.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long jwtExpirationInMs;

    public JwtTokenProvider(@Value("${jwt.secretKey}") String jwtSecretKey,
                            @Value("${jwt.expiration}") long jwtExpirationInMs) {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
        this.jwtExpirationInMs = jwtExpirationInMs;
    }

    // Generates a JWT token containing user information.
    public String generateToken(User user) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("userId", user.getId())
                .claim("userName", user.getName())
                .claim("userEmail", user.getEmail())
                .claim("userRole", user.getRole().name())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    // Validates a JWT token.
    // If token is valid, returns true.
    // If signature is invalid, expired, or malformed, returns false.
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            System.out.println("Jwt validation failed: " + e.getMessage());
            return false;
        }
    }

    // Helper to get all claims using modern .getPayload() instead of .getBody()
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Extracts userId from a valid JWT token.
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return ((Number) claims.get("userId")).longValue();
    }

    public String getUserNameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return (String) claims.get("userName");
    }

    public Role getUserRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String roleSt = (String) claims.get("userRole");

        return Role.valueOf(roleSt);
    }

    public Date getExpirationDateFromUser(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }
}
