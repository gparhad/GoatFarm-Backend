package com.goatfarm.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    private String fullName;
    private String userName;
    private String passwordHash;
    private String role; // ADMIN, FARMER, VETERINARIAN
    private String email;
    private String phone;

    @OneToOne(mappedBy = "farmer", cascade = CascadeType.ALL)
    private Farm farm;
}

