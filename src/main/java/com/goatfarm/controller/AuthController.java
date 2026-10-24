package com.goatfarm.controller;

import com.goatfarm.model.ForgotPasswordVerifyRequest;
import com.goatfarm.model.ForgotPasswordVerifyResponse;
import com.goatfarm.model.LoginRequest;
import com.goatfarm.model.MessageResponse;
import com.goatfarm.model.ResetPasswordRequest;
import com.goatfarm.model.TokenResponse;
import com.goatfarm.service.AuthService;
import com.goatfarm.service.ForgotPasswordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final ForgotPasswordService forgotPasswordService;

    public AuthController(AuthService authService, ForgotPasswordService forgotPasswordService) {
        this.authService = authService;
        this.forgotPasswordService = forgotPasswordService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(new TokenResponse(authService.login(loginRequest)));
    }

    /** Step 1 of password reset: verify email + phone and receive a one-time reset token. */
    @PostMapping("/forgot-password/verify")
    public ResponseEntity<ForgotPasswordVerifyResponse> verifyUser(
            @RequestBody ForgotPasswordVerifyRequest forgotPasswordVerifyRequest) {
        return ResponseEntity.ok(forgotPasswordService.verifyUser(
                forgotPasswordVerifyRequest.email(), forgotPasswordVerifyRequest.phone()));
    }

    /** Step 2 of password reset: set a new password using the reset token. */
    @PostMapping("/forgot-password/reset")
    public ResponseEntity<MessageResponse> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        forgotPasswordService.resetPassword(resetPasswordRequest.resetToken(), resetPasswordRequest.newPassword());
        return ResponseEntity.ok(new MessageResponse("Password updated successfully"));
    }

    @GetMapping("/health")
    public ResponseEntity<MessageResponse> healthCheck() {
        return ResponseEntity.ok(new MessageResponse("Service is up and running"));
    }
}
