package com.goatfarm.service;

import com.goatfarm.entity.Farm;
import com.goatfarm.entity.User;
import com.goatfarm.exception.InvalidCredentialException;
import com.goatfarm.model.LoginRequest;
import com.goatfarm.repository.UserRepository;
import com.goatfarm.security.JwtUtil;
import com.goatfarm.security.PasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Authentication use case: verifies credentials and issues the JWT. */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordService passwordService, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * @return a signed JWT
     * @throws InvalidCredentialException for any failure (unknown user, wrong password, missing input) without saying which
     */
    @Transactional
    public String login(LoginRequest loginRequest) {
        if (loginRequest == null
                || loginRequest.email() == null || loginRequest.email().isBlank()
                || loginRequest.password() == null) {
            log.warn("Login rejected: email or password missing");
            throw new InvalidCredentialException();
        }
        User user = userRepository.findByEmailIgnoreCase(loginRequest.email().trim())
                .filter(found -> found.getUserId() != null && found.getPasswordHash() != null)
                .orElseThrow(() -> {
                    log.warn("Login rejected: unknown user");
                    return new InvalidCredentialException();
                });

        if (!passwordService.matches(loginRequest.password(), user.getPasswordHash())) {
            log.warn("Login rejected: wrong password, userId={}", user.getUserId());
            throw new InvalidCredentialException();
        }
        if (passwordService.needsUpgrade(user.getPasswordHash())) {
            user.setPasswordHash(passwordService.encode(loginRequest.password()));
            userRepository.save(user);
            log.info("Password hash upgraded from legacy plaintext to BCrypt: userId={}", user.getUserId());
        }

        Farm farm = user.getFarm();
        log.info("Login succeeded: userId={}, farmId={}", user.getUserId(), farm != null ? farm.getFarmId() : null);
        return jwtUtil.generateToken(
                user.getUserId(),
                user.getEmail(),
                user.getFullName(),
                farm != null ? farm.getFarmId() : null,
                farm != null ? farm.getFarmName() : null);
    }
}
