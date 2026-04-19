package com.goatfarm.contoller;

import com.goatfarm.entity.HealthRecord;
import com.goatfarm.model.VaccinationRecordData;
import com.goatfarm.service.HealthRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/health")
public class HealthRecordController {
    @Autowired
    private HealthRecordService service;

    @PostMapping
    public ResponseEntity<HealthRecord> addRecord(@RequestBody HealthRecord record) {
        return ResponseEntity.ok(service.addRecord(record));
    }

    @GetMapping("/goat/{goatId}")
    public ResponseEntity<List<HealthRecord>> getRecords(@PathVariable Long goatId) {
        return ResponseEntity.ok(service.getRecordsByGoat(goatId));
    }

}

