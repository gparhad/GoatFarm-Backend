package com.goatfarm.repository;

import com.goatfarm.entity.PasswordResetRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetRequestRepository extends JpaRepository<PasswordResetRequest, Long> {

    /** {@code resetToken} is the SHA-256 hex of the token given to the client. */
    Optional<PasswordResetRequest> findByResetTokenAndUsedFalse(String resetToken);

    /** Atomically claims a token; returns 0 if somebody else already used it (prevents double use under concurrency). */
    @Modifying(flushAutomatically = true)
    @Query("update PasswordResetRequest r set r.used = true where r.id = :id and r.used = false")
    int markUsed(@Param("id") Long id);

    long deleteByExpiresAtBefore(Instant cutoff);
}
