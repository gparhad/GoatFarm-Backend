package com.goatfarm.repository;

import com.goatfarm.entity.Goat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GoatRepository extends JpaRepository<Goat, Long> {

    List<Goat> findByFarm_FarmId(Long farmId);

    Optional<Goat> findByTagNumberAndFarm_FarmId(String tagNumber, Long farmId);

    Optional<Goat> findByGoatIdAndFarm_FarmId(Long goatId, Long farmId);

    boolean existsByTagNumberAndFarm_FarmId(String tagNumber, Long farmId);

    /** Row-locks the goat so two concurrent breeding requests for the same goat are serialised. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from Goat g where g.tagNumber = :tagNumber and g.farm.farmId = :farmId")
    Optional<Goat> findForUpdateByTagNumberAndFarmId(@Param("tagNumber") String tagNumber,
                                                     @Param("farmId") Long farmId);
}
