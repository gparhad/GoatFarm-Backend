package com.goatfarm.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** A pending password reset. {@code resetToken} holds the SHA-256 of the token handed to the client, never the token itself. */
@Entity
@Table(name = "password_reset")
@Getter
@Setter
public class PasswordResetRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String resetToken;
    private Instant expiresAt;
    private boolean used;
    private Instant createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PasswordResetRequest other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return PasswordResetRequest.class.hashCode();
    }
}
