package com.goatfarm.service;

import com.goatfarm.entity.Farm;
import com.goatfarm.entity.User;
import com.goatfarm.exception.BusinessRuleViolationException;
import com.goatfarm.exception.ResourceNotFoundException;
import com.goatfarm.mapper.FarmMapper;
import com.goatfarm.model.FarmData;
import com.goatfarm.repository.FarmRepository;
import com.goatfarm.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class FarmService {

    private static final Logger log = LoggerFactory.getLogger(FarmService.class);

    private final FarmRepository farmRepository;
    private final UserRepository userRepository;

    public FarmService(FarmRepository farmRepository, UserRepository userRepository) {
        this.farmRepository = farmRepository;
        this.userRepository = userRepository;
    }

    /**
     * Registers a farm for a user. The farm id is always generated, never taken from the request, and a user
     * can own only one farm (previously a second attempt overwrote data or failed with a 500).
     */
    @Transactional
    public FarmData addFarm(FarmData farmData) {
        Long userId = farmData.getUserId();
        if (userId == null) {
            throw new BusinessRuleViolationException("userId is required to register farm");
        }
        User farmer = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (farmRepository.findByFarmerUserId(userId).isPresent()) {
            log.warn("Farm registration rejected: user already has a farm, userId={}", userId);
            throw new BusinessRuleViolationException("A farm is already registered for this user");
        }
        Farm farm = FarmMapper.toEntity(farmData);
        farm.setFarmer(farmer);
        Farm saved = farmRepository.save(farm);
        log.info("Farm registered: farmId={}, userId={}", saved.getFarmId(), userId);
        return FarmMapper.toDto(saved);
    }

    /** Returns an empty object when there is no farm (existing API behaviour). */
    public FarmData getFarmByFarmId(Long farmId) {
        return findFarm(farmId).map(FarmMapper::toDto).orElseGet(() -> {
            log.warn("Farm not found: farmId={}", farmId);
            return new FarmData();
        });
    }

    /** Only {@code size} and {@code goatTypes} are updatable (name and location are not, as before). */
    @Transactional
    public FarmData updateFarm(Long farmId, FarmData farmData) {
        Farm farm = findFarm(farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Farm not found: " + farmId));
        farm.setSize(farmData.getSize());
        farm.setGoatTypes(normalizeGoatTypes(farmData.getGoatTypes()));
        Farm saved = farmRepository.save(farm);
        log.info("Farm updated: farmId={}", farmId);
        return FarmMapper.toDto(saved);
    }

    private Optional<Farm> findFarm(Long farmId) {
        return farmId == null ? Optional.empty() : farmRepository.findById(farmId);
    }

    /** "BEETAL ,BOER" becomes "BEETAL, BOER". */
    static String normalizeGoatTypes(String goatTypes) {
        if (goatTypes == null) {
            return "";
        }
        return goatTypes.trim()
                .replaceAll("\\s*,\\s*", ", ")
                .replaceAll("^\\s*,|\\s*,$", "");
    }
}
