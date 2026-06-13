package com.goatfarm.contoller;

import com.goatfarm.exceptionHandler.InvalidCredentialException;
import com.goatfarm.model.*;
import com.goatfarm.service.ForgotPasswordService;
import com.goatfarm.service.UserService;
import com.goatfarm.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final ForgotPasswordService forgotPasswordService;

    public AuthController(UserService userService, JwtUtil jwtUtil, ForgotPasswordService forgotPasswordService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.forgotPasswordService = forgotPasswordService;
    }

    @PostMapping("/login")


    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest loginRequest) {

        if (loginRequest == null ||

                loginRequest.getUserName() == null || loginRequest.getUserName().isBlank() ||

                loginRequest.getPassword() == null) {

            throw new InvalidCredentialException();
        }

        UserData userData = userService.findByUsername(loginRequest.getUserName());

// same behavior: invalid if user missing or password missing

        if (userData == null || userData.getUserId() == null || userData.getPasswordHash() == null) {
            throw new InvalidCredentialException();
        }

// No functional change: plain compare as per your current login logic

        if (!userData.getPasswordHash().equals(loginRequest.getPassword())) {
            throw new InvalidCredentialException();

        }

        String token = jwtUtil.generateToken(

                userData.getUserId(),

                userData.getUserName(),

                userData.getFullName(),

                userData.getFarmId(),

                userData.getFarmName()

        );
        return ResponseEntity.ok((Map.of("Token", token)));
    }

    @PostMapping("/forgot-password/verify")
    public ResponseEntity<ForgotPasswordVerifyResponse> verifyForgotPasswordUser(@RequestBody ForgotPasswordVerifyRequest request) {

        ForgotPasswordVerifyResponse response =
                forgotPasswordService.verifyUser(request.getUsername(), request.getPhone());

        return ResponseEntity.ok(response);
    }

    /**
     * Step 2: Reset password using resetToken + newPassword
     * <p>
     * Frontend calls this from Setup New Password page
     */

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<Map<String, String>> resetPassword(
            @RequestBody ResetPasswordRequest request
    ) {

        forgotPasswordService.resetPassword(request.getResetToken(), request.getNewPassword());

        return ResponseEntity.ok(Map.of(
                "message", "Password updated successfully"
        ));
    }
}

