package com.goatfarm.repository;

import com.goatfarm.entity.BreedingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BreedingRecordRepository extends JpaRepository<BreedingRecord, Long> {
    List<BreedingRecord> findByGoatTagNumberAndFarm_FarmId(String goatTagNumber, Long farmId);

    List<BreedingRecord> findByBreederTagNumberAndFarm_FarmId(String breederTagNumber, Long farmId);

    Optional<BreedingRecord> findByGoatTagNumberAndPregnancyStatusAndFarm_FarmIdOrderByBreedingDateDesc(String goatTagNumber, String pregnancyStatus, Long farmId);

    boolean existsByGoatTagNumberAndPregnancyStatusAndFarm_FarmId(String goatTagNumber, String pregnancyStatus, Long farmId);

    @Query("""
            select b
            from BreedingRecord b
            where b.farm.farmId = :farmId
            and  b.pregnancyStatus = :status
            and b.expectedKiddingDate is not null
            and b.expectedKiddingDate between :from and :to
            order by b.expectedKiddingDate asc
            """)
    List<BreedingRecord> findDueDeliveriesInWindow(@Param("farmId") Long farmId, @Param("from") LocalDate from, @Param("to") LocalDate to, String status);
}
