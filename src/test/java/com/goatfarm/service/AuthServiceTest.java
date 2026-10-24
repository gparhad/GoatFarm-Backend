package com.goatfarm.service;

import com.goatfarm.entity.Farm;
import com.goatfarm.entity.User;
import com.goatfarm.exception.InvalidCredentialException;
import com.goatfarm.model.LoginRequest;
import com.goatfarm.repository.UserRepository;
import com.goatfarm.security.JwtUtil;
import com.goatfarm.security.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtil jwtUtil;

    private final PasswordService passwordService = new PasswordService(new BCryptPasswordEncoder());
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordService, jwtUtil);
        when(jwtUtil.generateToken(any(), any(), any(), any(), any())).thenReturn("jwt-token");
    }

    private User user(String email, String storedPassword) {
        User user = new User();
        user.setUserId(5L);
        user.setEmail(email);
        user.setFullName("Asha");
        user.setPasswordHash(storedPassword);
        return user;
    }

    @Test
    void legacyPlaintextAccountsCanLogInAndAreUpgradedToBcrypt() {
        User legacy = user("a@b.com", "oldpass1");
        when(userRepository.findByEmailIgnoreCase("a@b.com")).thenReturn(Optional.of(legacy));

        String token = authService.login(new LoginRequest("a@b.com", "oldpass1"));

        assertThat(token).isEqualTo("jwt-token");
        assertThat(legacy.getPasswordHash()).startsWith("$2");
        verify(userRepository).save(legacy);
    }

    @Test
    void bcryptAccountsAreNotRewrittenOnLogin() {
        User current = user("a@b.com", passwordService.encode("secret1"));
        when(userRepository.findByEmailIgnoreCase("a@b.com")).thenReturn(Optional.of(current));

        authService.login(new LoginRequest("a@b.com", "secret1"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void emailLookupIsTrimmedAndCaseInsensitive() {
        when(userRepository.findByEmailIgnoreCase("Asha@Example.com")).thenReturn(Optional.of(user("asha@example.com", "oldpass1")));

        assertThat(authService.login(new LoginRequest("  Asha@Example.com ", "oldpass1"))).isEqualTo("jwt-token");
    }

    @Test
    void tokenCarriesTheFarmWhenThereIsOne() {
        User withFarm = user("a@b.com", "oldpass1");
        Farm farm = new Farm();
        farm.setFarmId(9L);
        farm.setFarmName("Green Acres");
        withFarm.setFarm(farm);
        when(userRepository.findByEmailIgnoreCase("a@b.com")).thenReturn(Optional.of(withFarm));

        authService.login(new LoginRequest("a@b.com", "oldpass1"));

        verify(jwtUtil).generateToken(eq(5L), eq("a@b.com"), eq("Asha"), eq(9L), eq("Green Acres"));
    }

    @Test
    void everyFailureLooksTheSame() {
        when(userRepository.findByEmailIgnoreCase("a@b.com")).thenReturn(Optional.of(user("a@b.com", "oldpass1")));
        when(userRepository.findByEmailIgnoreCase("ghost@b.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("a@b.com", "wrong")))
                .isInstanceOf(InvalidCredentialException.class).hasMessage("Invalid credentials");
        assertThatThrownBy(() -> authService.login(new LoginRequest("ghost@b.com", "x")))
                .isInstanceOf(InvalidCredentialException.class).hasMessage("Invalid credentials");
        assertThatThrownBy(() -> authService.login(new LoginRequest("", "x"))).isInstanceOf(InvalidCredentialException.class);
        assertThatThrownBy(() -> authService.login(new LoginRequest("a@b.com", null))).isInstanceOf(InvalidCredentialException.class);
        assertThatThrownBy(() -> authService.login(null)).isInstanceOf(InvalidCredentialException.class);
    }
}
