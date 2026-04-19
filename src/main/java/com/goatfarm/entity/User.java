package com.goatfarm.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "Users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String username;
    private String passwordHash;
    private String role; // ADMIN, FARMER, VETERINARIAN
    private String email;
    private String phone;

    @OneToOne(mappedBy = "farmer", cascade = CascadeType.ALL)
//    @JsonManagedReference
    private Farm farm;
}

