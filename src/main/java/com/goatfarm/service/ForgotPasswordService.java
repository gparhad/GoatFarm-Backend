package com.goatfarm.service;
import com.goatfarm.entity.PasswordResetRequest;
import com.goatfarm.entity.User;
import com.goatfarm.model.ForgotPasswordVerifyResponse;
import com.goatfarm.repository.PasswordResetRequestRepository;
import com.goatfarm.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class ForgotPasswordService {

    private final UserRepository userRepository;
    private final PasswordResetRequestRepository passwordResetRequestRepository;

    public ForgotPasswordService(UserRepository userRepository, PasswordResetRequestRepository passwordResetRequestRepository) {
        this.userRepository = userRepository;
        this.passwordResetRequestRepository = passwordResetRequestRepository;
    }

    @Transactional
    public ForgotPasswordVerifyResponse verifyUser(String username, String phone) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone number is required");
        }

        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (user.getPhone() == null || !user.getPhone().equals(phone)) {
            throw new IllegalArgumentException("Username and phone number do not match");
        }

        String resetToken = UUID.randomUUID().toString();

        PasswordResetRequest request = new PasswordResetRequest();
        request.setUserId(user.getUserId());
        request.setResetToken(resetToken);
        request.setExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES)); // valid for 10 mins
        request.setUsed(false);

        passwordResetRequestRepository.save(request);

        return new ForgotPasswordVerifyResponse(
                true,
                "User verified successfully",
                resetToken
        );
    }

    /**
     * Step 2: Reset password using reset token
     */
    @Transactional
    public void resetPassword(String resetToken, String newPassword) {
        if (resetToken == null || resetToken.isBlank()) {
            throw new IllegalArgumentException("Reset token is required");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        PasswordResetRequest request = passwordResetRequestRepository.findByResetTokenAndUsedFalse(resetToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or used reset token"));

        if (Instant.now().isAfter(request.getExpiresAt())) {
            throw new IllegalArgumentException("Reset token expired");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        /**
         * IMPORTANT:
         * Your current login compares raw password with passwordHash directly:
         * userData.getPasswordHash().equals(loginRequest.getPassword())
         *
         * So for now, to avoid breaking login / UI, we store the new password directly.
         * Later we should migrate login + registration to BCrypt hashing.
         */
        user.setPasswordHash(newPassword);
        userRepository.save(user);

        request.setUsed(true);
        passwordResetRequestRepository.save(request);
    }
}