package com.goatfarm.controller;

import com.goatfarm.domain.VaccinationRules;
import com.goatfarm.model.AuthUser;
import com.goatfarm.model.DueVaccinationData;
import com.goatfarm.model.VaccinationRecordData;
import com.goatfarm.service.VaccinationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vaccinations")
public class VaccinationController {

    private final VaccinationService vaccinationService;

    public VaccinationController(VaccinationService vaccinationService) {
        this.vaccinationService = vaccinationService;
    }

    @PostMapping
    public ResponseEntity<VaccinationRecordData> addVaccinationRecord(
            @RequestBody VaccinationRecordData vaccinationRecordData,
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vaccinationService.addVaccinationRecord(vaccinationRecordData, authUser.farmId()));
    }

    @GetMapping("/history/{tagNumber}")
    public ResponseEntity<List<VaccinationRecordData>> getVaccinationHistoryByTagNumber(
            @PathVariable String tagNumber,
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(vaccinationService.getVaccinationHistoryByTagNumber(tagNumber, authUser.farmId()));
    }

    /** The path says "tomorrow" but it has always returned what is due in 7 days; kept for client compatibility. */
    @GetMapping("/schedule/tomorrow")
    public ResponseEntity<List<DueVaccinationData>> getVaccinationsDueSoon(@AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(vaccinationService.getVaccinationsDueSoon(authUser.farmId()));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<VaccinationRecordData>> getUpcomingVaccinations(@AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(
                vaccinationService.getUpcomingVaccinations(authUser.farmId(), VaccinationRules.UPCOMING_WINDOW_DAYS));
    }
}
