package com.goatfarm.service;

import com.goatfarm.entity.PasswordResetRequest;
import com.goatfarm.entity.User;
import com.goatfarm.exception.BusinessRuleViolationException;
import com.goatfarm.exception.ResourceNotFoundException;
import com.goatfarm.model.ForgotPasswordVerifyResponse;
import com.goatfarm.repository.PasswordResetRequestRepository;
import com.goatfarm.repository.UserRepository;
import com.goatfarm.security.PasswordService;
import com.goatfarm.support.Hashing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Service
public class ForgotPasswordService {

    private static final Logger log = LoggerFactory.getLogger(ForgotPasswordService.class);

    private static final Duration TOKEN_LIFETIME = Duration.ofMinutes(10);
    private static final int TOKEN_BYTES = 32;
    private static final String MISMATCH_MESSAGE = "Username and phone number do not match";
    private static final String INVALID_TOKEN_MESSAGE = "Invalid or used reset token";

    private final UserRepository userRepository;
    private final PasswordResetRequestRepository resetRepository;
    private final PasswordService passwordService;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    public ForgotPasswordService(UserRepository userRepository,
                                 PasswordResetRequestRepository resetRepository,
                                 PasswordService passwordService,
                                 Clock clock) {
        this.userRepository = userRepository;
        this.resetRepository = resetRepository;
        this.passwordService = passwordService;
        this.clock = clock;
    }

    /**
     * Step 1: verify the user by email + phone and hand out a one-time reset token.
     * An unknown email and a wrong phone give the same answer, so the endpoint cannot be used to discover accounts.
     */
    @Transactional
    public ForgotPasswordVerifyResponse verifyUser(String email, String phone) {
        if (email == null || email.isBlank()) {
            throw new BusinessRuleViolationException("Username is required");
        }
        if (phone == null || phone.isBlank()) {
            throw new BusinessRuleViolationException("Phone number is required");
        }
        Instant now = Instant.now(clock);
        resetRepository.deleteByExpiresAtBefore(now);

        User user = userRepository.findByEmailIgnoreCase(email.trim())
                .filter(found -> found.getPhone() != null && found.getPhone().equals(phone))
                .orElseThrow(() -> {
                    log.warn("Password reset verification failed: email/phone do not match an account");
                    return new BusinessRuleViolationException(MISMATCH_MESSAGE);
                });

        String resetToken = newToken();
        PasswordResetRequest request = new PasswordResetRequest();
        request.setUserId(user.getUserId());
        request.setResetToken(Hashing.sha256Hex(resetToken));
        request.setExpiresAt(now.plus(TOKEN_LIFETIME));
        request.setCreatedAt(now);
        request.setUsed(false);
        resetRepository.save(request);
        log.info("Password reset token issued: userId={}, validMinutes={}", user.getUserId(), TOKEN_LIFETIME.toMinutes());

        return new ForgotPasswordVerifyResponse(true, "User verified successfully", resetToken);
    }

    /** Step 2: set a new password using the token from step 1. */
    @Transactional
    public void resetPassword(String resetToken, String newPassword) {
        if (resetToken == null || resetToken.isBlank()) {
            throw new BusinessRuleViolationException("Reset token is required");
        }
        passwordService.validateNewPassword(newPassword);

        PasswordResetRequest request = resetRepository.findByResetTokenAndUsedFalse(Hashing.sha256Hex(resetToken))
                .orElseThrow(() -> {
                    log.warn("Password reset rejected: unknown or already used token");
                    return new BusinessRuleViolationException(INVALID_TOKEN_MESSAGE);
                });
        if (Instant.now(clock).isAfter(request.getExpiresAt())) {
            log.warn("Password reset rejected: token expired, userId={}", request.getUserId());
            throw new BusinessRuleViolationException("Reset token expired");
        }
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Atomic claim: a concurrent second use of the same token updates 0 rows and is rejected.
        if (resetRepository.markUsed(request.getId()) == 0) {
            log.warn("Password reset rejected: token was used concurrently, userId={}", request.getUserId());
            throw new BusinessRuleViolationException(INVALID_TOKEN_MESSAGE);
        }
        user.setPasswordHash(passwordService.encode(newPassword));
        userRepository.save(user);
        log.info("Password reset completed: userId={}", user.getUserId());
    }

    private String newToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
