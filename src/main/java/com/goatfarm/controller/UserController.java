package com.goatfarm.controller;

import com.goatfarm.model.AuthUser;
import com.goatfarm.model.UserData;
import com.goatfarm.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** Public sign-up endpoint. */
    @PostMapping("/addUser")
    public ResponseEntity<UserData> addUser(@RequestBody UserData userData) {
        return ResponseEntity.ok(userService.addUser(userData));
    }

    /** Profile of the logged-in user; an empty object (200) if the user no longer exists, as before. */
    @GetMapping
    public ResponseEntity<UserData> getUserById(@AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(userService.getUserById(authUser.userId()));
    }

    @PutMapping
    public ResponseEntity<UserData> updateUser(@AuthenticationPrincipal AuthUser authUser,
                                               @RequestBody UserData userData) {
        return ResponseEntity.ok(userService.updateUser(userData, authUser.userId()));
    }
}
