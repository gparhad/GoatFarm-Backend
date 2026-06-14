package com.goatfarm.contoller;

import com.goatfarm.model.AuthUser;
import com.goatfarm.model.BreedingRecordData;
import com.goatfarm.model.UpcomingDeliveryDateData;
import com.goatfarm.service.BreedingRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/breeding")
public class BreedingRecordController {
    @Autowired
    private BreedingRecordService breedingService;

    @PostMapping
    public ResponseEntity<BreedingRecordData> addRecord(
            @RequestBody BreedingRecordData record,
            @AuthenticationPrincipal AuthUser authUser) {
        BreedingRecordData breedingRecordData = breedingService.addBreedingRecord(record, authUser.getFarmId());
        return ResponseEntity.status(201).body(breedingRecordData);
    }

    @GetMapping("/breeder/{tagNumber}")
    public ResponseEntity<List<BreedingRecordData>> getRecordsByBreederTag(
            @PathVariable String tagNumber,
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(breedingService.getRecordsByBreederTag(tagNumber, authUser.getFarmId()));
    }

    @GetMapping("/history/{tagNumber}")
    public ResponseEntity<List<BreedingRecordData>> getRecordsByGoatTag(
            @PathVariable String tagNumber,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.ok(
                breedingService.getRecordsByGoatTag(tagNumber, authUser.getFarmId())
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<BreedingRecordData> updateBreedingRecord(
            @PathVariable Long id,
            @RequestBody BreedingRecordData dto,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        BreedingRecordData updated = breedingService.updateBreedingRecord(id, dto, authUser.getFarmId());
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/check-inbreeding")
    public ResponseEntity<Map<String, Object>> checkInbreeding(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        String goatTag = request.get("goatTagNumber");
        String breederTag = request.get("breederTagNumber");
        return ResponseEntity.ok(
                breedingService.checkInbreeding(goatTag, breederTag, authUser.getFarmId())
        );
    }

    @GetMapping("/latest-pregnant/{goatTag}")
    public ResponseEntity<Map<String, Object>> getLatestPregnant(
            @PathVariable String goatTag,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.ok(
                breedingService.getLatestPregnantRecord(goatTag, authUser.getFarmId())
        );
    }

    @PutMapping("/delivery/{goatTag}")
    public ResponseEntity<Map<String, Object>> updateDelivery(
            @PathVariable String goatTag,
            @RequestBody BreedingRecordData dto,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.ok(
                breedingService.updateDeliveryDetails(goatTag, dto, authUser.getFarmId())
        );
    }

    @GetMapping("/upcoming-deliveries")
    public ResponseEntity<List<UpcomingDeliveryDateData>> getUpcomingDeliveries(
            @RequestParam(defaultValue = "3") int pastDays,
            @RequestParam(defaultValue = "7") int futureDays,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.ok(
                breedingService.getUpcomingDeliveriesWindow(authUser.getFarmId(), pastDays, futureDays)
        );
    }

}

