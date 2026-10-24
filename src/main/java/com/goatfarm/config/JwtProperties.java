package com.goatfarm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * JWT settings.
 *
 * @param secret       Base64-encoded HMAC key, at least 256 bits (env {@code JWT_SECRET}); never hardcode it
 * @param expirationMs token lifetime in milliseconds (default 7 days, the effective value used before)
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String secret, @DefaultValue("604800000") long expirationMs) {
}
