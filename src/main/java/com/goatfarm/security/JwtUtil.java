package com.goatfarm.security;

import com.goatfarm.config.JwtProperties;
import com.goatfarm.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/** Creates and validates JWTs. The signing secret comes from configuration, never from source code. */
@Component
public class JwtUtil {

    private final SecretKey signingKey;
    private final Duration tokenLifetime;
    private final Clock clock;

    public JwtUtil(JwtProperties properties, Clock clock) {
        this.signingKey = buildKey(properties.secret());
        this.tokenLifetime = Duration.ofMillis(properties.expirationMs());
        this.clock = clock;
    }

    public String generateToken(Long userId, String email, String fullName, Long farmId, String farmName) {
        Instant now = clock.instant();
        return Jwts.builder()
                .claim("userId", userId)
                .claim("email", email)
                .claim("farmId", farmId)
                .claim("fullName", fullName)
                .claim("farmName", farmName)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(tokenLifetime)))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * @throws InvalidTokenException if the token is expired, malformed or its signature does not match
     */
    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .clock(() -> Date.from(clock.instant()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            throw new InvalidTokenException("JWT token has expired", ex);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new InvalidTokenException("Invalid JWT token", ex);
        }
    }

    private static SecretKey buildKey(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "jwt.secret is not configured. Set the JWT_SECRET environment variable to a Base64-encoded "
                            + "key of at least 256 bits (e.g. `openssl rand -base64 48`).");
        }
        try {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret.trim()));
        } catch (RuntimeException ex) {
            throw new IllegalStateException("jwt.secret must be a Base64-encoded key of at least 256 bits.", ex);
        }
    }
}
