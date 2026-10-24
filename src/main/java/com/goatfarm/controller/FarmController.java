package com.goatfarm.controller;

import com.goatfarm.model.AuthUser;
import com.goatfarm.model.FarmData;
import com.goatfarm.service.FarmService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/farm")
public class FarmController {

    private final FarmService farmService;

    public FarmController(FarmService farmService) {
        this.farmService = farmService;
    }

    /** Public on purpose: it is called during sign-up, before the user has a token. */
    @PostMapping("/addFarm")
    public ResponseEntity<FarmData> addFarm(@RequestBody FarmData farmData) {
        return ResponseEntity.ok(farmService.addFarm(farmData));
    }

    @GetMapping
    public ResponseEntity<FarmData> getFarmByFarmId(@AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(farmService.getFarmByFarmId(authUser.farmId()));
    }

    @PutMapping
    public ResponseEntity<FarmData> updateFarm(@AuthenticationPrincipal AuthUser authUser,
                                               @RequestBody FarmData farmData) {
        return ResponseEntity.ok(farmService.updateFarm(authUser.farmId(), farmData));
    }
}
