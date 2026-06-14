package com.goatfarm.service;

import com.goatfarm.entity.Farm;
import com.goatfarm.entity.User;
import com.goatfarm.mapper.FarmMapper;
import com.goatfarm.model.FarmData;
import com.goatfarm.model.FarmUpdateData;
import com.goatfarm.repository.FarmRepository;
import com.goatfarm.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FarmService {

    private final FarmRepository farmRepository;
    private final UserRepository userRepository;

    public FarmService(FarmRepository farmRepository, UserRepository userRepository) {
        this.farmRepository = farmRepository;
        this.userRepository = userRepository;
    }

    /**
     * Register a farm for a user.
     * No functional change: still uses farmData.getUserId() and associates farm.farmer.
     */
    public Farm registerFarm(FarmData farmData) {
        Farm farm = FarmMapper.toFarm(farmData);
        Long userId = farmData.getUserId();

        if (userId == null) {
            // Keeps behavior safe; will be handled by ControllerAdvice as 400 if you add handler
            throw new IllegalArgumentException("userId is required to register farm");
        }

        User farmer = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        farm.setFarmer(farmer);
        return farmRepository.save(farm);
    }

    /**
     * Get farm by farmer (user) id.
     * No functional change: returns empty FarmData if not found (same as before).
     */
    public FarmData getFarmByFarmer(Long userId) {
        Optional<Farm> farmOptional = farmRepository.findByFarmerUserId(userId);
        return farmOptional.map(FarmMapper::toFarmDTO).orElseGet(FarmData::new);
    }

    /**
     * Get farm by farmId.
     * No functional change: returns empty FarmData if not found (same as before).
     */
    public FarmData getFarmByFarmId(Long farmId) {
        Optional<Farm> farmOptional = farmRepository.findById(farmId);
        return farmOptional.map(FarmMapper::toFarmDTO).orElseGet(FarmData::new);
    }

    /**
     * Update farm fields.
     * No functional change: throws EntityNotFoundException if farm not found (same behavior intent).
     */
    @Transactional
    public FarmData updateFarm(Long farmId, FarmUpdateData req) {
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new EntityNotFoundException("Farm not found: " + farmId));

        // Update farm fields
        farm.setFarmName(req.getFarmName());
        farm.setLocation(req.getLocation());
        farm.setSize(req.getSize());
        farm.setGoatTypes(normalizeGoatTypes(req.getGoatType()));

        Farm saved = farmRepository.save(farm);

        // Return same shape as GET
        return FarmMapper.toFarmDTO(saved);
    }

    /**
     * Normalize goat types: "BEETAL ,BOER" -> "BEETAL, BOER"
     * No functional change.
     */
    private String normalizeGoatTypes(String goatTypes) {
        if (goatTypes == null) return "";
        return goatTypes.trim()
                .replaceAll("\\s*,\\s*", ", ")
                .replaceAll("^\\s*,|\\s*,$", "");
    }
}