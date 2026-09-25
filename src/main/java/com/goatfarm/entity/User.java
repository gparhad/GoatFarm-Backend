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
    @Column(name = "fullname")
    private String fullName;
    @Column(name = "password_hash")
    private String passwordHash;
    @Column(name = "email", unique = true)
    private String email;
    private String phone;

    @OneToOne(mappedBy = "farmer", cascade = CascadeType.ALL)
    private Farm farm;
}

