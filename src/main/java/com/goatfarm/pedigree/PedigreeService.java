package com.goatfarm.pedigree;

import com.goatfarm.repository.GoatRepository;
import org.springframework.stereotype.Component;

/** Loads a {@link PedigreeSnapshot} for a farm with a single query. */
@Component
public class PedigreeService {

    private final GoatRepository goatRepository;

    public PedigreeService(GoatRepository goatRepository) {
        this.goatRepository = goatRepository;
    }

    public PedigreeSnapshot snapshot(Long farmId) {
        if (farmId == null) {
            return PedigreeSnapshot.empty();
        }
        return PedigreeSnapshot.of(goatRepository.findByFarm_FarmId(farmId));
    }
}
