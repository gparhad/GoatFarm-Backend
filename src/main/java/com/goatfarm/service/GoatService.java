package com.goatfarm.service;

import com.goatfarm.entity.Farm;
import com.goatfarm.entity.Goat;
import com.goatfarm.mapper.GoatMapper;
import com.goatfarm.model.GoatData;
import com.goatfarm.model.GoatHistoryData;
import com.goatfarm.model.GoatTreeNode;
import com.goatfarm.repository.FarmRepository;
import com.goatfarm.repository.GoatRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
public class GoatService {

    private static final int MAX_GENERATION_DEPTH = 5;
    private final GoatRepository goatRepository;
    private final FarmRepository farmRepository;
    private final BreedingRecordService breedingRecordService;
    private final VaccinationService vaccinationService;

    public GoatService(GoatRepository goatRepository, FarmRepository farmRepository, BreedingRecordService breedingRecordService, VaccinationService vaccinationService) {
        this.goatRepository = goatRepository;
        this.farmRepository = farmRepository;
        this.breedingRecordService = breedingRecordService;
        this.vaccinationService = vaccinationService;
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

    public GoatHistoryData getGoatHistoryDataByTagNumberAndFarmId(String tagNumber, Long farmId, int depth) {
        // 1. Validate root goat existence first
        GoatData goatData = getGoatByTagNumberAndFarmId(tagNumber, farmId);
        if (goatData == null) {
            throw new EntityNotFoundException("Goat not found with tag: " + tagNumber);
        }

        GoatHistoryData historyData = new GoatHistoryData();
        historyData.setGoatDetails(goatData);
        historyData.setBreedingHistoryData(breedingRecordService.getRecordsByGoatTag(tagNumber, farmId));
        historyData.setVaccinationHistoryData(vaccinationService.getVaccinationHistoryByTagNumber(tagNumber, farmId));
        historyData.setGoatFamilyHierarchy(getGoatHierarchy(tagNumber, farmId, depth));

        return historyData;
    }


    /**
     * Retrieves the pedigree/hierarchy tree for a specific goat up to the specified depth.
     * Defaulting to MAX_GENERATION_DEPTH if depth is not provided or <= 0.
     */
    @Transactional
    public GoatTreeNode getGoatHierarchy(String goatTag, Long farmId, Integer depth) {
        int maxDepth = (depth != null && depth > 0) ? depth : MAX_GENERATION_DEPTH;
        return buildHierarchyInternal(goatTag, maxDepth, new HashSet<>(), farmId);
    }

    private GoatTreeNode buildHierarchyInternal(String tag, int depth, Set<String> currentPath, Long farmId) {
        if (tag == null || tag.trim().isEmpty() || depth <= 0) {
            return null;
        }

        // Check for an actual circular reference in the current ancestry chain
        if (currentPath.contains(tag)) {
            GoatTreeNode cycleNode = new GoatTreeNode();
            cycleNode.setTagNumber(tag);
            cycleNode.setCycleDetected(true);
            cycleNode.setMissingReason("Pedigree loop/cycle detected");
            return cycleNode;
        }

        Goat goat = goatRepository.findByTagNumberAndFarm_FarmId(tag, farmId).orElse(null);

        // If tag was referenced as a parent but record does not exist in DB
        if (goat == null) {
            GoatTreeNode missingNode = new GoatTreeNode();
            missingNode.setTagNumber(tag);
            missingNode.setMissingReason("Parent record not registered in database");
            return missingNode;
        }

        GoatTreeNode node = new GoatTreeNode();
        node.setTagNumber(goat.getTagNumber());
        node.setBreed(goat.getBreed());
        node.setGender(goat.getGender());
        node.setBirthDate(goat.getBirthDate());
        node.setWeight(goat.getWeight());
        node.setHeight(goat.getHeight());
        node.setCycleDetected(false);

        // Track path for loop detection
        currentPath.add(tag);

        // Recursively build father and mother branches
        node.setFather(buildHierarchyInternal(goat.getFatherTagNumber(), depth - 1, currentPath, farmId));
        node.setMother(buildHierarchyInternal(goat.getMotherTagNumber(), depth - 1, currentPath, farmId));

        // Backtrack to allow common ancestors across separate branches (e.g., line-breeding)
        currentPath.remove(tag);

        return node;
    }
}