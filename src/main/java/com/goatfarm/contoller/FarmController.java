package com.goatfarm.contoller;

import com.goatfarm.entity.Farm;
import com.goatfarm.model.AuthUser;
import com.goatfarm.model.FarmData;
import com.goatfarm.model.FarmUpdateData;
import com.goatfarm.service.FarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/farm")
public class FarmController {

    @Autowired
    private FarmService farmService;

    @PostMapping("/addFarm")
    public ResponseEntity<Farm> registerFarm(@RequestBody FarmData farm) {
        return ResponseEntity.ok(farmService.registerFarm(farm));
    }

    @GetMapping
    public ResponseEntity<FarmData> getFarmById(@AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(farmService.getFarmByFarmId(authUser.getFarmId()));
    }

    @PutMapping
    public ResponseEntity<FarmData> updateFarm(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody FarmUpdateData farmData
    ) {
        return ResponseEntity.ok(farmService.updateFarm(authUser.getFarmId(), farmData));
    }
}

