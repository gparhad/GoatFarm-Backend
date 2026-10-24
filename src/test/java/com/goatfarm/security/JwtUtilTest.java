package com.goatfarm.security;

import com.goatfarm.config.JwtProperties;
import com.goatfarm.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private static final long SEVEN_DAYS_MS = 7L * 24 * 3600 * 1000;
    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    private static String randomSecret() {
        byte[] bytes = new byte[48];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static JwtUtil jwt(String secret, Instant now) {
        return new JwtUtil(new JwtProperties(secret, SEVEN_DAYS_MS), Clock.fixed(now, ZoneOffset.UTC));
    }

    @Test
    void roundTripsTheClaims() {
        JwtUtil jwt = jwt(randomSecret(), NOW);

        Claims claims = jwt.validateToken(jwt.generateToken(7L, "a@b.com", "Asha", 3L, "Farm"));

        assertThat(((Number) claims.get("userId")).longValue()).isEqualTo(7L);
        assertThat(((Number) claims.get("farmId")).longValue()).isEqualTo(3L);
        assertThat(claims.get("email")).isEqualTo("a@b.com");
        assertThat(claims.get("farmName")).isEqualTo("Farm");
    }

    @Test
    void aUserWithoutAFarmGetsATokenWithoutFarmClaims() {
        JwtUtil jwt = jwt(randomSecret(), NOW);

        Claims claims = jwt.validateToken(jwt.generateToken(7L, "a@b.com", "Asha", null, null));

        assertThat(claims.get("farmId")).isNull();
    }

    @Test
    void expiredTokensAreRejectedWithATypedException() {
        String secret = randomSecret();
        String token = jwt(secret, NOW).generateToken(1L, "a@b.com", "A", 1L, "F");
        JwtUtil eightDaysLater = jwt(secret, NOW.plusSeconds(8 * 24 * 3600));

        assertThatThrownBy(() -> eightDaysLater.validateToken(token))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("JWT token has expired");
    }

    @Test
    void tamperedOrForeignTokensAreRejected() {
        JwtUtil jwt = jwt(randomSecret(), NOW);
        String foreign = jwt(randomSecret(), NOW).generateToken(1L, "a@b.com", "A", 1L, "F");

        assertThatThrownBy(() -> jwt.validateToken(foreign)).isInstanceOf(InvalidTokenException.class).hasMessage("Invalid JWT token");
        assertThatThrownBy(() -> jwt.validateToken("not-a-jwt")).isInstanceOf(InvalidTokenException.class);
        assertThatThrownBy(() -> jwt.validateToken("")).isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void refusesToStartWithoutAUsableSecret() {
        assertThatThrownBy(() -> jwt(null, NOW)).isInstanceOf(IllegalStateException.class).hasMessageContaining("JWT_SECRET");
        assertThatThrownBy(() -> jwt("  ", NOW)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> jwt(Base64.getEncoder().encodeToString(new byte[8]), NOW)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> jwt("%%%not-base64%%%", NOW)).isInstanceOf(IllegalStateException.class);
    }
}
