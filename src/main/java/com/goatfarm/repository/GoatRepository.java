package com.goatfarm.repository;

import com.goatfarm.entity.Goat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GoatRepository extends JpaRepository<Goat, Long> {
    List<Goat> findByFarm_FarmId(Long farmId);

    Optional<Goat> findByTagNumberAndFarm_FarmId(String tagNumber, Long farmId);

    Optional<Goat> findByGenderAndFarm_FarmId(String gender, Long farmId);

}


