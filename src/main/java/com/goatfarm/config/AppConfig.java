package com.goatfarm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class AppConfig {

    /**
     * Single time source for the whole application, so "today" is consistent and testable.
     * Defaults to the JVM zone (previous behaviour). Set {@code APP_TIME_ZONE=Asia/Kolkata}
     * on a UTC host so "today"/"upcoming" windows follow the farmer's calendar.
     */
    @Bean
    public Clock clock(@Value("${app.time-zone:}") String timeZone) {
        return timeZone == null || timeZone.isBlank()
                ? Clock.systemDefaultZone()
                : Clock.system(ZoneId.of(timeZone.trim()));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
