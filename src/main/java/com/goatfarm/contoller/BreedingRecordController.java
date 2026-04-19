package com.goatfarm.contoller;

import com.goatfarm.model.BreedingRecordData;
import com.goatfarm.service.BreedingRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/breeding")
public class BreedingRecordController {
    @Autowired
    private BreedingRecordService service;

    @PostMapping
    public ResponseEntity<BreedingRecordData> addRecord(@RequestBody BreedingRecordData record) {
        return ResponseEntity.ok(service.addBreedingRecord(record));
    }

    @GetMapping("/breeder/{tagNumber}")
    public ResponseEntity<List<BreedingRecordData>> getRecordsByBreederTag(@PathVariable String tagNumber) {
        return ResponseEntity.ok(service.getRecordsByBreederTag(tagNumber));
    }

    @GetMapping("/goat/{tagNumber}")
    public ResponseEntity<List<BreedingRecordData>> getRecordsByGoatTag(@PathVariable String tagNumber) {
        return ResponseEntity.ok(service.getRecordsByGoatTag(tagNumber));
    }
    @PutMapping("/{id}")
    public ResponseEntity<BreedingRecordData> updateBreedingRecord(
            @PathVariable Long id,
            @RequestBody BreedingRecordData dto) {
        BreedingRecordData updated = service.updateBreedingRecord(id, dto);
        return ResponseEntity.ok(updated);
    }

}

