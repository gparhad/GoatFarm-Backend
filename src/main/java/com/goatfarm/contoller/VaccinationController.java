package com.goatfarm.contoller;

import com.goatfarm.model.VaccinationRecordData;
import com.goatfarm.service.VaccinationService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vaccinations")
public class VaccinationController {

    @Autowired
    private VaccinationService vaccinationService;

    @PostMapping
    public ResponseEntity<VaccinationRecordData> addVaccination(@RequestBody VaccinationRecordData dto) {
        VaccinationRecordData saved = vaccinationService.addVaccinationRecord(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/history/{tagNumber}")
    public ResponseEntity<List<VaccinationRecordData>> getVaccinationHistory(@PathVariable String tagNumber) {
        return ResponseEntity.ok(vaccinationService.getVaccinationHistoryByTag(tagNumber));
    }

    @GetMapping("/schedule/tomorrow")
    public ResponseEntity<List<Map<String, Object>>> getTomorrowVaccinations() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Claims claims = (Claims) authentication.getDetails();

        Long farmId = Long.valueOf(claims.get("farmId").toString());
        List<Map<String, Object>> schedules = vaccinationService.getTomorrowVaccinationsByFarm2(farmId);

        return ResponseEntity.ok(schedules);
    }
}

