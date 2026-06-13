package com.goatfarm.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {
    private final String SECRET = "JHGJHfkljlwhsflhhjk65gjkhjl86968JKHKHdchjhklh6547887chgjlho";

    public String generateToken(Long userId, String username, String fullName, Long farmId, String farmName) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("username", username)
                .claim("farmId", farmId)   // new claim
                .claim("fullName", fullName)
                .claim("farmName", farmName)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(7, ChronoUnit.DAYS)))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims validateToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET));
        try {
            return Jwts.parser()
                    .verifyWith(key) // Replacement for setSigningKey
                    .build()
                    .parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException ex) {
            throw new RuntimeException("JWT token has expired", ex);
        } catch (JwtException ex) {
            throw new RuntimeException("Invalid JWT token", ex);
        }
    }
}

