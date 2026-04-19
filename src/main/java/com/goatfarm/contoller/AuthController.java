package com.goatfarm.contoller;

import com.goatfarm.entity.LoginRequest;
import com.goatfarm.entity.User;
import com.goatfarm.model.UserData;
import com.goatfarm.service.UserService;
import com.goatfarm.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        UserData userData = userService.findByUsername(loginRequest.getUsername());

        if ( userData.getPasswordHash().equals(loginRequest.getPassword())) {
            String token = jwtUtil.generateToken(userData.getUserId(), userData.getUsername(), userData.getFarmId());
            return ResponseEntity.ok(Map.of("token", token));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }
}

