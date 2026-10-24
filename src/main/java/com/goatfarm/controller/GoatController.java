package com.goatfarm.controller;

import com.goatfarm.model.AuthUser;
import com.goatfarm.model.GoatData;
import com.goatfarm.model.GoatHistoryData;
import com.goatfarm.model.MessageResponse;
import com.goatfarm.service.GoatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/goat")
public class GoatController {

    private final GoatService goatService;

    public GoatController(GoatService goatService) {
        this.goatService = goatService;
    }

    @PostMapping
    public ResponseEntity<GoatData> addGoat(@Valid @RequestBody GoatData goatData,
                                            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(goatService.addGoat(goatData, authUser.farmId()));
    }

    @GetMapping("/allGoats")
    public ResponseEntity<List<GoatData>> getGoatsByFarm(@AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(goatService.getGoatsByFarm(authUser.farmId()));
    }

    @GetMapping("/tag/{tagNumber}")
    public ResponseEntity<GoatData> getGoatByTagNumber(@PathVariable String tagNumber,
                                                       @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(goatService.getGoatByTagNumber(tagNumber, authUser.farmId()));
    }

    @PutMapping("/update/{tagNumber}")
    public ResponseEntity<GoatData> updateGoatByTagNumber(@PathVariable String tagNumber,
                                                          @RequestBody GoatData goatData,
                                                          @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(goatService.updateGoatByTagNumber(tagNumber, goatData, authUser.farmId()));
    }

    @DeleteMapping("/delete/{tagNumber}")
    public ResponseEntity<MessageResponse> deleteGoatByTagNumber(@PathVariable String tagNumber,
                                                                 @AuthenticationPrincipal AuthUser authUser) {
        goatService.deleteGoatByTagNumber(tagNumber, authUser.farmId());
        return ResponseEntity.ok(new MessageResponse("Goat deleted successfully"));
    }

    @GetMapping("/details/{tagNumber}")
    public ResponseEntity<GoatHistoryData> getGoatHistoryByTagNumber(@PathVariable String tagNumber,
                                                                     @RequestParam(defaultValue = "5") int depth,
                                                                     @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(goatService.getGoatHistoryByTagNumber(tagNumber, authUser.farmId(), depth));
    }
}
