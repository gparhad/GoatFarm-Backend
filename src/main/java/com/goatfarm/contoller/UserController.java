package com.goatfarm.contoller;

import com.goatfarm.model.AuthUser;
import com.goatfarm.model.UserData;
import com.goatfarm.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    // Constructor injection (best practice)
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Create new user.
     * No functional change: still returns 200 OK with saved user.
     */
    @PostMapping("/addUser")
    public ResponseEntity<UserData> createUser(@RequestBody UserData user) {
        UserData savedUser = userService.createUser(user);
        return ResponseEntity.ok(savedUser);
    }

    /**
     * Get logged-in user's profile using JWT principal.
     * No functional change: returns 200 OK with UserData (even if empty object).
     * Note:
     * - If you later want 404 when user not found, we can switch to throwing EntityNotFoundException
     * and let ControllerAdvice return 404. But that could be a behavioral change.
     */
    @GetMapping
    public ResponseEntity<UserData> getMyProfile(@AuthenticationPrincipal AuthUser authUser) {
        UserData user = userService.getUserById(authUser.getUserId());
        return ResponseEntity.ok(user);
    }

    /**
     * Update logged-in user's profile.
     * No functional change: returns 200 OK with updated user.
     */
    @PutMapping
    public ResponseEntity<UserData> updateUser(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody UserData user
    ) {
        UserData savedUser = userService.updateUser(user, authUser.getUserId());
        return ResponseEntity.ok(savedUser);
    }
}
