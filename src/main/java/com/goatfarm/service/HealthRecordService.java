package com.goatfarm.service;

import com.goatfarm.entity.HealthRecord;
import com.goatfarm.repository.HealthRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HealthRecordService {
    @Autowired
    private HealthRecordRepository repo;

    public HealthRecord addRecord(HealthRecord record) {
        return repo.save(record);
    }

    public List<HealthRecord> getRecordsByGoat(Long goatId) {
        return repo.findByGoatGoatId(goatId);
    }

}

