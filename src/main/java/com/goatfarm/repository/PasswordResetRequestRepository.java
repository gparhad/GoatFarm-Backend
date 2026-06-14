package com.goatfarm.repository;

import com.goatfarm.entity.PasswordResetRequest;
import io.jsonwebtoken.security.Jwks;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetRequestRepository extends JpaRepository<PasswordResetRequest, Long> {
    Optional<PasswordResetRequest> findByResetTokenAndUsedFalse(String resetToken);
}
