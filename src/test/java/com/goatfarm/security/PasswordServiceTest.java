package com.goatfarm.security;

import com.goatfarm.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordServiceTest {

    private final PasswordService service = new PasswordService(new BCryptPasswordEncoder());

    @Test
    void encodedPasswordsAreBcryptAndVerify() {
        String hash = service.encode("secret1");

        assertThat(hash).startsWith("$2");
        assertThat(service.matches("secret1", hash)).isTrue();
        assertThat(service.matches("wrong", hash)).isFalse();
        assertThat(service.needsUpgrade(hash)).isFalse();
    }

    @Test
    void legacyPlaintextStillWorksAndIsFlaggedForUpgrade() {
        assertThat(service.matches("password123", "password123")).isTrue();
        assertThat(service.matches("password124", "password123")).isFalse();
        assertThat(service.needsUpgrade("password123")).isTrue();
    }

    @Test
    void nullsNeverMatch() {
        assertThat(service.matches(null, "x")).isFalse();
        assertThat(service.matches("x", null)).isFalse();
    }

    @Test
    void newPasswordPolicy() {
        assertThatCode(() -> service.validateNewPassword("secret1")).doesNotThrowAnyException();
        assertThatThrownBy(() -> service.validateNewPassword("abc"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Password must be at least 6 characters");
        assertThatThrownBy(() -> service.validateNewPassword(null))
                .isInstanceOf(BusinessRuleViolationException.class);
        assertThatThrownBy(() -> service.validateNewPassword("x".repeat(73)))
                .hasMessage("Password must be at most 72 bytes");
    }
}
