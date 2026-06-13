package com.goatfarm.repository;

import io.jsonwebtoken.security.Jwks;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetRecordRepository extends JpaRepository<PasswordResetRequest, Long> {
    Optional<PasswordResetRequest> findByResetTokenAndUsedFalse(String resetToken);
}
