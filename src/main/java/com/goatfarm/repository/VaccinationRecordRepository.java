package com.goatfarm.repository;

import com.goatfarm.entity.Goat;
import com.goatfarm.entity.VaccinationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface VaccinationRecordRepository extends JpaRepository<VaccinationRecord, Long> {

    List<VaccinationRecord> findByGoatOrderByVaccinationDateAsc(Goat goat);

    /** Farm scoping always goes through the goat, so legacy rows with a null {@code farm_id} column are included. */
    @Query("""
            select vr
            from VaccinationRecord vr
            join fetch vr.goat g
            where g.farm.farmId = :farmId
            and vr.nextVaccinationDate = :date
            """)
    List<VaccinationRecord> findDueVaccinationsByFarmAndDate(@Param("farmId") Long farmId,
                                                             @Param("date") LocalDate date);

    @Query("""
            select vr
            from VaccinationRecord vr
            join fetch vr.goat g
            where g.farm.farmId = :farmId
            and vr.nextVaccinationDate is not null
            and vr.nextVaccinationDate between :from and :to
            order by vr.nextVaccinationDate asc
            """)
    List<VaccinationRecord> findUpcomingVaccinations(@Param("farmId") Long farmId,
                                                     @Param("from") LocalDate from,
                                                     @Param("to") LocalDate to);
}
