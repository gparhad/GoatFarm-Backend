package com.goatfarm.repository;

import com.goatfarm.entity.BreedingRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BreedingRecordRepository extends JpaRepository<BreedingRecord, Long> {
    List<BreedingRecord> findByGoatGoatId(Long goatId);
    List<BreedingRecord> findByGoatTagNumber(String goatTagNumber);
    ;
    List<BreedingRecord> findByBreederTagNumber(String breederTagNumber);
}
