package com.goatfarm.repository;

import com.goatfarm.entity.Goat;
import com.goatfarm.entity.VaccinationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface VaccinationRecordRepository extends JpaRepository<VaccinationRecord, Long> {
    List<VaccinationRecord> findByGoat(Goat goat);

    List<VaccinationRecord> findByGoatTagNumber(String tagNumber);

    @Query("SELECT vr FROM VaccinationRecord vr " +
            "JOIN vr.goat g " +
            "WHERE g.farm.farmId = :farmId " +
            "AND vr.nextVaccinationDate = :date")
    List<VaccinationRecord> findDueVaccinationsByFarmAndDate(@Param("farmId") Long farmId,
                                                             @Param("date") LocalDate date);

}
