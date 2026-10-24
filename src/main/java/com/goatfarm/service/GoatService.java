package com.goatfarm.service;

import com.goatfarm.domain.BreedingRules;
import com.goatfarm.entity.Farm;
import com.goatfarm.entity.Goat;
import com.goatfarm.exception.BusinessRuleViolationException;
import com.goatfarm.exception.ResourceNotFoundException;
import com.goatfarm.mapper.GoatMapper;
import com.goatfarm.model.GoatData;
import com.goatfarm.model.GoatHistoryData;
import com.goatfarm.model.GoatTreeNode;
import com.goatfarm.pedigree.PedigreeService;
import com.goatfarm.pedigree.PedigreeTreeBuilder;
import com.goatfarm.repository.BreedingRecordRepository;
import com.goatfarm.repository.FarmRepository;
import com.goatfarm.repository.GoatRepository;
import com.goatfarm.support.GoatLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class GoatService {

    private static final Logger log = LoggerFactory.getLogger(GoatService.class);

    private final GoatRepository goatRepository;
    private final FarmRepository farmRepository;
    private final BreedingRecordRepository breedingRecordRepository;
    private final GoatLookup goatLookup;
    private final PedigreeService pedigreeService;
    private final BreedingRecordService breedingRecordService;
    private final VaccinationService vaccinationService;

    public GoatService(GoatRepository goatRepository,
                       FarmRepository farmRepository,
                       BreedingRecordRepository breedingRecordRepository,
                       GoatLookup goatLookup,
                       PedigreeService pedigreeService,
                       BreedingRecordService breedingRecordService,
                       VaccinationService vaccinationService) {
        this.goatRepository = goatRepository;
        this.farmRepository = farmRepository;
        this.breedingRecordRepository = breedingRecordRepository;
        this.goatLookup = goatLookup;
        this.pedigreeService = pedigreeService;
        this.breedingRecordService = breedingRecordService;
        this.vaccinationService = vaccinationService;
    }

    /** Adds a goat to the farm. A tag number can only be used once per farm. */
    @Transactional
    public GoatData addGoat(GoatData goatData, Long farmId) {
        Farm farm = Optional.ofNullable(farmId).flatMap(farmRepository::findFarmByFarmId)
                .orElseThrow(() -> new ResourceNotFoundException("Farm not found with farmId: " + farmId));
        if (goatRepository.existsByTagNumberAndFarm_FarmId(goatData.getTagNumber(), farmId)) {
            log.warn("Goat rejected: tag already exists, tagNumber={}, farmId={}", goatData.getTagNumber(), farmId);
            throw new BusinessRuleViolationException(
                    "Goat with tag '" + goatData.getTagNumber() + "' already exists in this farm");
        }
        Goat saved = goatRepository.save(GoatMapper.toEntity(goatData, farm));
        log.info("Goat added: goatId={}, tagNumber={}, farmId={}", saved.getGoatId(), saved.getTagNumber(), farmId);
        return GoatMapper.toDtoWithRecords(saved);
    }

    public List<GoatData> getGoatsByFarm(Long farmId) {
        if (farmId == null) {
            return List.of();
        }
        List<GoatData> goats = goatRepository.findByFarm_FarmId(farmId).stream()
                .map(GoatMapper::toDtoWithRecords)
                .toList();
        log.debug("Goats loaded: farmId={}, count={}", farmId, goats.size());
        return goats;
    }

    public GoatData getGoatByTagNumber(String tagNumber, Long farmId) {
        return GoatMapper.toDto(goatLookup.requireByTagNumber(tagNumber, farmId));
    }

    @Transactional
    public GoatData updateGoatByTagNumber(String tagNumber, GoatData goatData, Long farmId) {
        Goat goat = goatLookup.requireByTagNumber(tagNumber, farmId);
        GoatMapper.applyUpdate(goat, goatData);
        Goat saved = goatRepository.save(goat);
        log.info("Goat updated: tagNumber={}, farmId={}", tagNumber, farmId);
        return GoatMapper.toDtoWithRecords(saved);
    }

    /**
     * Deletes the goat together with its own vaccination and breeding records. Records of other goats that
     * name it as the sire are kept (they retain the sire's tag number) instead of blocking the delete.
     */
    @Transactional
    public void deleteGoatByTagNumber(String tagNumber, Long farmId) {
        Goat goat = goatLookup.requireByTagNumber(tagNumber, farmId);
        int detached = breedingRecordRepository.clearMate(goat.getGoatId());
        goatRepository.delete(goat);
        log.info("Goat deleted: tagNumber={}, farmId={}, breedingRecordsKeptWithoutSire={}", tagNumber, farmId, detached);
    }

    public GoatHistoryData getGoatHistoryByTagNumber(String tagNumber, Long farmId, int depth) {
        Goat goat = goatLookup.requireByTagNumber(tagNumber, farmId);
        GoatHistoryData history = new GoatHistoryData();
        history.setGoatDetails(GoatMapper.toDto(goat));
        history.setBreedingHistoryData(breedingRecordService.getBreedingRecordsByGoatTagNumber(tagNumber, farmId));
        history.setVaccinationHistoryData(vaccinationService.getVaccinationHistoryByGoat(goat));
        history.setGoatFamilyHierarchy(getGoatHierarchy(tagNumber, farmId, depth));
        return history;
    }

    /** Pedigree tree up to {@code depth} generations; a null or non-positive depth means the default. */
    public GoatTreeNode getGoatHierarchy(String tagNumber, Long farmId, Integer depth) {
        int maxDepth = (depth != null && depth > 0) ? depth : BreedingRules.DEFAULT_PEDIGREE_DEPTH;
        return PedigreeTreeBuilder.build(tagNumber, maxDepth, pedigreeService.snapshot(farmId));
    }
}
