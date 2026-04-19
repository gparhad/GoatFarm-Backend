package com.goatfarm.contoller;

import com.goatfarm.entity.Farm;
import com.goatfarm.model.FarmData;
import com.goatfarm.service.FarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farms")
public class FarmController {
    @Autowired
    private FarmService farmService;

    @PostMapping
    public ResponseEntity<String> registerFarm(@RequestBody FarmData farm) {
        return ResponseEntity.ok(farmService.registerFarm(farm));
    }

    @GetMapping("/{farmerId}")
    public ResponseEntity<FarmData> getFarmsByFarmer(@PathVariable Long farmerId) {
        return ResponseEntity.ok(farmService.getFarmsByFarmer(farmerId));
    }
}

