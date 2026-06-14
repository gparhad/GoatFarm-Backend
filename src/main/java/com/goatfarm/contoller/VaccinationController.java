package com.goatfarm.contoller;

import com.goatfarm.model.AuthUser;
import com.goatfarm.model.VaccinationRecordData;
import com.goatfarm.service.VaccinationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vaccinations")
public class VaccinationController {

    private final VaccinationService vaccinationService;

    public VaccinationController(VaccinationService vaccinationService) {
        this.vaccinationService = vaccinationService;
    }

    /**
     * Add vaccination for a goat in logged-in user's farm.
     * No UI change: same endpoint path, returns 201 Created.
     */
    @PostMapping
    public ResponseEntity<VaccinationRecordData> addVaccination(
            @RequestBody VaccinationRecordData dto,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        VaccinationRecordData saved = vaccinationService.addVaccinationRecord(dto, authUser.getFarmId());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Vaccination history by goat tag (scoped to logged-in user's farm).
     * No UI change: same endpoint.
     */
    @GetMapping("/history/{tagNumber}")
    public ResponseEntity<List<VaccinationRecordData>> getVaccinationHistory(
            @PathVariable String tagNumber,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.ok(
                vaccinationService.getVaccinationHistoryByTagNumber(tagNumber, authUser.getFarmId())
        );
    }

    /**
     * Existing endpoint name says tomorrow, but your service uses now+7.
     * No functional change: keep same behavior; only farm scoping cleaned.
     */
    @GetMapping("/schedule/tomorrow")
    public ResponseEntity<List<Map<String, Object>>> getTomorrowVaccinations(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.ok(
                vaccinationService.getTomorrowVaccinationsByFarm2(authUser.getFarmId())
        );
    }

    /**
     * Upcoming vaccinations endpoint currently uses path farmId.
     * No UI change: keep the same URL.
     * Security: validate farmId matches token farmId (prevents cross-farm access).
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<VaccinationRecordData>> getUpcomingVaccinations(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        List<VaccinationRecordData> result = vaccinationService.getUpcomingVaccinations(authUser.getFarmId(), 7);
        return ResponseEntity.ok(result);
    }

    /*
     * Optional (recommended future): query param based endpoint
     * Example: /api/vaccinations/upcoming?days=7
     * Not required for UI now, so not enabling unless you want.
     */
    // @GetMapping("/upcoming")
    // public ResponseEntity<List<VaccinationRecordData>> getUpcomingVaccinations(
    //         @RequestParam(defaultValue = "7") int days,
    //         @AuthenticationPrincipal AuthUser authUser
    // ) {
    //     return ResponseEntity.ok(vaccinationService.getUpcomingVaccinations(authUser.getFarmId(), days));
    // }
}

