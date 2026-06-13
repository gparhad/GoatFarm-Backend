package com.goatfarm.contoller;
import com.goatfarm.entity.Goat;
import com.goatfarm.model.GoatData;
import com.goatfarm.service.GoatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goats")
public class GoatController {
    @Autowired private GoatService goatService;

    @PostMapping
    public ResponseEntity<Goat> addGoat(@RequestBody GoatData goat) {
        return ResponseEntity.ok(goatService.addGoat(goat));
    }

    @GetMapping("/farm/{farmId}")
    public ResponseEntity<List<Goat>> getGoatsByFarm(@PathVariable Long farmId) {
        return ResponseEntity.ok(goatService.getGoatsByFarm(farmId));
    }

    @GetMapping("/tag/{tagNumber}")
    public ResponseEntity<GoatData> getGoatByTagNumber(@PathVariable String tagNumber) {
        GoatData response = goatService.getGoatByTagNumber(tagNumber);
        return ResponseEntity.ok(response);
    }


}

