package com.goatfarm.repository;

import com.goatfarm.entity.Farm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FarmRepository extends JpaRepository<Farm, Long> {

    Optional<Farm> findByFarmerUserId(Long farmerId);

    Optional<Farm> findFarmByFarmId(Long farmId);
}
