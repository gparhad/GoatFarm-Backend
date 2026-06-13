package com.goatfarm.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "password_reset")
@Data
public class PasswordResetRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String resetToken;
    private Instant expiresAt;
    private boolean used = false;

    private Instant createdAt = Instant.now();
}
