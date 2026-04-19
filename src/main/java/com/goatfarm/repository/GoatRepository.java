package com.goatfarm.repository;

import com.goatfarm.entity.Goat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GoatRepository extends JpaRepository<Goat, Long> {
    List<Goat> findByFarmFarmId(Long farmId);

    Optional<Goat> findByTagNumber(String tagNumber);
}


