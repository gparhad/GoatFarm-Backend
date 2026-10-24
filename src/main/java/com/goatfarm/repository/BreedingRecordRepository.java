package com.goatfarm.repository;

import com.goatfarm.entity.BreedingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BreedingRecordRepository extends JpaRepository<BreedingRecord, Long> {

    List<BreedingRecord> findByGoatTagNumberAndFarm_FarmId(String goatTagNumber, Long farmId);

    List<BreedingRecord> findByBreederTagNumberAndFarm_FarmId(String breederTagNumber, Long farmId);

    Optional<BreedingRecord> findByBreedingIdAndFarm_FarmId(Long breedingId, Long farmId);

    /** Newest matching record; tolerant of historical duplicates (a plain Optional finder would throw). */
    Optional<BreedingRecord> findFirstByGoatTagNumberAndPregnancyStatusAndFarm_FarmIdOrderByBreedingIdDesc(
            String goatTagNumber, String pregnancyStatus, Long farmId);

    boolean existsByGoatTagNumberAndPregnancyStatusAndFarm_FarmId(String goatTagNumber, String pregnancyStatus, Long farmId);

    @Query("""
            select b
            from BreedingRecord b
            where b.farm.farmId = :farmId
            and b.pregnancyStatus = :status
            and b.expectedKiddingDate is not null
            and b.expectedKiddingDate between :from and :to
            order by b.expectedKiddingDate asc
            """)
    List<BreedingRecord> findDueDeliveriesInWindow(@Param("farmId") Long farmId,
                                                   @Param("from") LocalDate from,
                                                   @Param("to") LocalDate to,
                                                   @Param("status") String status);

    /** Detaches a goat from records where it appears as the sire so the goat can be deleted. The tag number stays on the record. */
    @Modifying(flushAutomatically = true)
    @Query("update BreedingRecord b set b.mate = null where b.mate.goatId = :goatId")
    int clearMate(@Param("goatId") Long goatId);
}
