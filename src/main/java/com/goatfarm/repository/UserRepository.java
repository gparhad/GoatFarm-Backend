package com.goatfarm.repository;

import com.goatfarm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmail(String email);

    /** Used during updates to exclude the current user's own email. */
    boolean existsByEmailAndUserIdNot(String email, Long userId);
}
