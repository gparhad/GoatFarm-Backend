package com.goatfarm.controller;

import com.goatfarm.model.ApiResult;
import com.goatfarm.model.AuthUser;
import com.goatfarm.model.BreedingRecordData;
import com.goatfarm.model.InbreedingCheckRequest;
import com.goatfarm.model.InbreedingCheckResponse;
import com.goatfarm.service.BreedingRecordService;
import com.goatfarm.service.InbreedingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/api/breeding")
public class BreedingRecordController {

    private final BreedingRecordService breedingRecordService;
    private final InbreedingService inbreedingService;

    public BreedingRecordController(BreedingRecordService breedingRecordService, InbreedingService inbreedingService) {
        this.breedingRecordService = breedingRecordService;
        this.inbreedingService = inbreedingService;
    }

    @PostMapping
    public ResponseEntity<BreedingRecordData> addBreedingRecord(@Valid @RequestBody BreedingRecordData breedingRecordData,
                                                                @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.status(201).body(breedingRecordService.addBreedingRecord(breedingRecordData, authUser.farmId()));
    }

    @GetMapping("/breeder/{breederTagNumber}")
    public ResponseEntity<List<BreedingRecordData>> getBreedingRecordsByBreederTagNumber(
            @PathVariable String breederTagNumber,
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(breedingRecordService.getBreedingRecordsByBreederTagNumber(breederTagNumber, authUser.farmId()));
    }

    @GetMapping("/history/{goatTagNumber}")
    public ResponseEntity<List<BreedingRecordData>> getBreedingRecordsByGoatTagNumber(
            @PathVariable String goatTagNumber,
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(breedingRecordService.getBreedingRecordsByGoatTagNumber(goatTagNumber, authUser.farmId()));
    }

    @PutMapping("/{breedingId}")
    public ResponseEntity<BreedingRecordData> updateBreedingRecord(@PathVariable Long breedingId,
                                                                   @RequestBody BreedingRecordData breedingRecordData,
                                                                   @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(breedingRecordService.updateBreedingRecord(breedingId, breedingRecordData, authUser.farmId()));
    }

    @PostMapping("/check-inbreeding")
    public ResponseEntity<InbreedingCheckResponse> checkInbreeding(
            @Valid @RequestBody InbreedingCheckRequest inbreedingCheckRequest,
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(inbreedingService.checkInbreeding(
                inbreedingCheckRequest.goatTagNumber(), inbreedingCheckRequest.breederTagNumber(), authUser.farmId()));
    }

    @GetMapping("/latest-pregnant/{goatTagNumber}")
    public ResponseEntity<ApiResult<BreedingRecordData>> getLatestPregnantBreedingRecord(
            @PathVariable String goatTagNumber,
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(breedingRecordService.getLatestPregnantBreedingRecord(goatTagNumber, authUser.farmId()));
    }

    @PutMapping("/delivery/{goatTagNumber}")
    public ResponseEntity<ApiResult<BreedingRecordData>> updateDeliveryDetails(
            @PathVariable String goatTagNumber,
            @RequestBody BreedingRecordData breedingRecordData,
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(breedingRecordService.updateDeliveryDetails(goatTagNumber, breedingRecordData, authUser.farmId()));
    }

    @GetMapping("/upcoming-deliveries")
    public ResponseEntity<List<BreedingRecordData>> getUpcomingDeliveries(
            @RequestParam(defaultValue = "3") int pastDays,
            @RequestParam(defaultValue = "7") int futureDays,
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(breedingRecordService.getUpcomingDeliveries(authUser.farmId(), pastDays, futureDays));
    }
}
