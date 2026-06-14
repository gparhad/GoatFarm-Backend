package com.goatfarm.service;

import com.goatfarm.entity.Farm;
import com.goatfarm.entity.Goat;
import com.goatfarm.mapper.GoatMapper;
import com.goatfarm.model.GoatData;
import com.goatfarm.repository.FarmRepository;
import com.goatfarm.repository.GoatRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoatService {

    private final GoatRepository goatRepository;
    private final FarmRepository farmRepository;

    public GoatService(GoatRepository goatRepository, FarmRepository farmRepository) {
        this.goatRepository = goatRepository;
        this.farmRepository = farmRepository;
    }

    /**
     * Add goat to a farm.
     * No functional change: still returns goat entity
     */
    public Goat addGoat(GoatData goatData, Long farmId) {
        Farm farm = farmRepository.findFarmByFarmId(farmId)
                .orElseThrow(() -> new EntityNotFoundException("Farm not found with farmId: " + farmId));
        Goat goatEntity = GoatMapper.toEntity(goatData, farm);
        // Save and return saved entity (safer, ensures id is set)
        return goatRepository.save(goatEntity);
    }

    /**
     * Fetch all goats for a farm.
     * No functional change
     */
    public List<Goat> getGoatsByFarm(Long farmId) {
        // ensure repository method exists (see note below)
        return goatRepository.findByFarm_FarmId(farmId);
    }

    /**
     * Fetch goat by tagNumber scoped to farm.
     * No functional change
     */
    public GoatData getGoatByTagNumberAndFarmId(String tagNumber, Long farmId) {
        Goat goat = goatRepository.findByTagNumberAndFarm_FarmId(tagNumber, farmId)
                .orElseThrow(() -> new EntityNotFoundException("Goat not found with tag: " + tagNumber));
        return GoatMapper.toDto(goat);
    }

    /**
     * Update goat by tagNumber scoped to farm.
     * No functional change (same updated fields)
     * Ensure repository method exists (name updates fields)
     */
    public Goat updateGoatByTagNumberAndFarmId(String tagNumber, GoatData updatedGoat, Long farmId) {
        Goat existing = goatRepository.findByTagNumberAndFarm_FarmId(tagNumber, farmId)
                .orElseThrow(() -> new EntityNotFoundException("Goat not found with tag: " + tagNumber));

        // Update fields (same behavior as your existing implementation)
        existing.setBreed(updatedGoat.getBreed());
        existing.setGender(updatedGoat.getGender());
        existing.setBirthDate(updatedGoat.getBirthDate());
        existing.setWeight(updatedGoat.getWeight());
        existing.setHealthStatus(updatedGoat.getHealthStatus());
        existing.setFatherTagNumber(updatedGoat.getFatherTagNumber());
        existing.setMotherTagNumber(updatedGoat.getMotherTagNumber());

        // NEW fields
        existing.setHeight(updatedGoat.getHeight());
        existing.setMilkPerDay(updatedGoat.getMilkPerDay());
        existing.setLastKidCount(updatedGoat.getLastKidCount());

        return goatRepository.save(existing);
    }

    /**
     * Delete goat by tagNumber scoped to farm.
     * No functional change
     */
    public void deleteByTagNumberAndFarmId(String tagNumber, Long farmId) {
        Goat goat = goatRepository.findByTagNumberAndFarm_FarmId(tagNumber, farmId)
                .orElseThrow(() -> new EntityNotFoundException("Goat not found with tag: " + tagNumber));
        goatRepository.delete(goat);
    }
}