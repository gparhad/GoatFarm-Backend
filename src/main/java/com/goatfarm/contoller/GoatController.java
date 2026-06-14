package com.goatfarm.contoller;

import com.goatfarm.entity.Goat;
import com.goatfarm.model.AuthUser;
import com.goatfarm.model.GoatData;
import com.goatfarm.service.GoatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goat")
public class GoatController {

    private final GoatService goatService;

    public GoatController(GoatService goatService) {
        this.goatService = goatService;
    }

    /**
     * Add goat scoped to logged-in user's farm.
     * No functional change: returns 200 OK with Goat
     */
    @PostMapping
    public ResponseEntity<Goat> addGoat(
            @RequestBody GoatData goat,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.ok(goatService.addGoat(goat, authUser.getFarmId()));
    }

    /**
     * Fetch all goats for logged-in user's farm.
     * No functional change: returns 200 OK with List<Goat>
     */
    @GetMapping("/allGoats")
    public ResponseEntity<List<Goat>> getGoatsByFarm(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.ok(goatService.getGoatsByFarm(authUser.getFarmId()));
    }

    /**
     * Fetch goat by tagNumber scoped to farm.
     * No functional change: returns 200 OK with GoatData
     */
    @GetMapping("/tag/{tagNumber}")
    public ResponseEntity<GoatData> getGoatByTagNumber(
            @PathVariable String tagNumber,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.ok(goatService.getGoatByTagNumberAndFarmId(tagNumber, authUser.getFarmId()));
    }

    /**
     * Update goat by tagNumber scoped to farm.
     * No functional change: returns 200 OK with updated Goat
     * Errors handled by ControllerAdvice (404 for not found etc.)
     */
    @PutMapping("/update/{tagNumber}")
    public ResponseEntity<Goat> updateGoat(
            @PathVariable String tagNumber,
            @RequestBody GoatData goat,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.ok(goatService.updateGoatByTagNumberAndFarmId(tagNumber, goat, authUser.getFarmId()));
    }

    /**
     * Delete goat by tagNumber scoped to farm.
     * No functional change: returns 200 OK with message
     */
    @DeleteMapping("/delete/{tagNumber}")
    public ResponseEntity<Map<String, String>> deleteGoat(
            @PathVariable String tagNumber,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        goatService.deleteByTagNumberAndFarmId(tagNumber, authUser.getFarmId());
        return ResponseEntity.ok(Map.of("message", "Goat deleted successfully"));
    }
}

