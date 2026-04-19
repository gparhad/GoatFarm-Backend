package com.goatfarm.repository;

import com.goatfarm.entity.Farm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FarmRepository extends JpaRepository<Farm, Long> {
    Optional<Farm> findByFarmerUserId(Long farmerId);

    @Query("SELECT f FROM Farm f WHERE f.farmer.userId = :userId")
    Optional<Farm> findFarmByFarmerId(@Param("userId") Long userId);


//    List<Farm> findByFarmerId(Long userId);
}
