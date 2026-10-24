package com.goatfarm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

/**
 * CORS settings. The default {@code *} keeps the previous (permissive) behaviour so existing
 * deployments keep working; set {@code APP_CORS_ALLOWED_ORIGINS} to your real frontend origin(s) in production.
 */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(@DefaultValue("*") List<String> allowedOriginPatterns,
                             @DefaultValue("true") boolean allowCredentials) {
}
