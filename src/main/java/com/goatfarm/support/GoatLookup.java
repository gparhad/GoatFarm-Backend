package com.goatfarm.support;

import com.goatfarm.entity.Goat;
import com.goatfarm.exception.ResourceNotFoundException;
import com.goatfarm.repository.GoatRepository;
import org.springframework.stereotype.Component;

/**
 * "Find this goat in this farm or fail" in one place. Callers can pass their own message because the
 * existing API reports slightly different texts per endpoint and those texts are part of the contract.
 */
@Component
public class GoatLookup {

    private final GoatRepository goatRepository;

    public GoatLookup(GoatRepository goatRepository) {
        this.goatRepository = goatRepository;
    }

    /** Standard "Goat not found with tag: X" failure used by the goat and vaccination endpoints. */
    public Goat requireByTagNumber(String tagNumber, Long farmId) {
        return require(tagNumber, farmId, "Goat not found with tag: " + tagNumber);
    }

    public Goat require(String tagNumber, Long farmId, String notFoundMessage) {
        if (tagNumber == null || farmId == null) {
            throw new ResourceNotFoundException(notFoundMessage);
        }
        return goatRepository.findByTagNumberAndFarm_FarmId(tagNumber, farmId)
                .orElseThrow(() -> new ResourceNotFoundException(notFoundMessage));
    }
}
