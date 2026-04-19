package com.goatfarm.service;

import com.goatfarm.entity.Farm;
import com.goatfarm.entity.Goat;
import com.goatfarm.mapper.GoatMapper;
import com.goatfarm.model.GoatData;
import com.goatfarm.repository.GoatRepository;
import com.goatfarm.util.FarmUtil;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GoatService {
    @Autowired
    private GoatRepository goatRepository;

    @Autowired
    FarmUtil farmUtil;

    public Goat addGoat(GoatData goatData) {
        Farm farm = farmUtil.getFarmByFarmerId();
        Goat goatEntity = GoatMapper.toEntity(goatData, farm);
        goatRepository.save(goatEntity);
        return goatEntity;
    }

    public List<Goat> getGoatsByFarm(Long farmId) {
        return goatRepository.findByFarmFarmId(farmId);
    }

    public GoatData getGoatByTagNumber(String tagNumber) {

        Goat goat = goatRepository.findByTagNumber(tagNumber)
                .orElseThrow(() -> new EntityNotFoundException("Goat not found with tag: " + tagNumber));

        GoatData goatData = GoatMapper.toDto(goat);
        return goatData;

    }
}

