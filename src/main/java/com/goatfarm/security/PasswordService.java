package com.goatfarm.security;

import com.goatfarm.exception.BusinessRuleViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Password hashing and verification.
 *
 * <p>New and reset passwords are stored as BCrypt. Accounts created before the refactor still hold a
 * plaintext value in {@code password_hash}; those are accepted once (compared in constant time) and
 * upgraded to BCrypt by {@code AuthService} on the next successful login, so nobody gets locked out.
 */
@Component
public class PasswordService {

    private static final int MIN_LENGTH = 6;
    /** BCrypt only uses the first 72 bytes of the input. */
    private static final int MAX_BYTES = 72;

    private final PasswordEncoder encoder;

    public PasswordService(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String stored) {
        if (rawPassword == null || stored == null) {
            return false;
        }
        if (isBcrypt(stored)) {
            return encoder.matches(rawPassword, stored);
        }
        return MessageDigest.isEqual(
                rawPassword.getBytes(StandardCharsets.UTF_8),
                stored.getBytes(StandardCharsets.UTF_8));
    }

    /** True for legacy plaintext values that should be re-stored as BCrypt. */
    public boolean needsUpgrade(String stored) {
        return stored != null && !isBcrypt(stored);
    }

    public void validateNewPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < MIN_LENGTH) {
            throw new BusinessRuleViolationException("Password must be at least " + MIN_LENGTH + " characters");
        }
        if (rawPassword.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            throw new BusinessRuleViolationException("Password must be at most " + MAX_BYTES + " bytes");
        }
    }

    private static boolean isBcrypt(String stored) {
        return stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$");
    }
}
