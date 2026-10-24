package com.goatfarm.service;

import com.goatfarm.entity.User;
import com.goatfarm.exception.BusinessRuleViolationException;
import com.goatfarm.exception.ResourceNotFoundException;
import com.goatfarm.mapper.UserMapper;
import com.goatfarm.model.UserData;
import com.goatfarm.repository.UserRepository;
import com.goatfarm.security.PasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public UserService(UserRepository userRepository, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    /**
     * Registers a user. The request's {@code passwordHash} field carries the plain password; it is stored as BCrypt.
     */
    @Transactional
    public UserData addUser(UserData userData) {
        String email = normalizeEmail(userData.getEmail());
        if (email != null && userRepository.existsByEmail(email)) {
            log.warn("User registration rejected: email already in use");
            throw new BusinessRuleViolationException("Email '" + email + "' is already in use.");
        }
        passwordService.validateNewPassword(userData.getPasswordHash());
        User saved = userRepository.save(
                UserMapper.toEntity(userData, email, passwordService.encode(userData.getPasswordHash())));
        log.info("User registered: userId={}", saved.getUserId());
        return UserMapper.toDto(saved, true);
    }

    @Transactional
    public UserData updateUser(UserData userData, Long userId) {
        String email = normalizeEmail(userData.getEmail());
        if (email != null && userRepository.existsByEmailAndUserIdNot(email, userId)) {
            log.warn("User update rejected: email already used by another user, userId={}", userId);
            throw new BusinessRuleViolationException("Email '" + email + "' is already in use by another user.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        UserMapper.applyUpdate(user, userData, email);
        User saved = userRepository.save(user);
        log.info("User updated: userId={}", userId);
        return UserMapper.toDto(saved, false);
    }

    /**
     * Returns an empty object when the user does not exist (existing API behaviour).
     */
    public UserData getUserById(Long userId) {
        if (userId == null) {
            return new UserData();
        }
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            return UserMapper.toDto(userOptional.get(), false);
        } else {
            log.warn("User not found: userId={}", userId);
            return new UserData();
        }

    }

    static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    public UserData findByUserName(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .map(user -> UserMapper.toDto(user, true))
                .orElseGet(UserData::new);
    }
}
