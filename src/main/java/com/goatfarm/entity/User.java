package com.goatfarm.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "fullname")
    private String fullName;

    /** BCrypt hash. Legacy rows may still contain plaintext until the user's next login (see PasswordService). */
    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "email", unique = true)
    private String email;

    private String phone;

    /** Inverse side of a one-to-one: Hibernate cannot make it lazy without bytecode enhancement. */
    @OneToOne(mappedBy = "farmer", cascade = CascadeType.ALL)
    private Farm farm;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return getUserId() != null && getUserId().equals(other.getUserId());
    }

    @Override
    public int hashCode() {
        return User.class.hashCode();
    }
}
