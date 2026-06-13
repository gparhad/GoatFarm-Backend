package com.goatfarm.service;

import com.goatfarm.entity.Farm;
import com.goatfarm.entity.User;
import com.goatfarm.mapper.EntityMapper;
import com.goatfarm.model.FarmData;
import com.goatfarm.repository.FarmRepository;
import com.goatfarm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FarmService {
    @Autowired
    private FarmRepository farmRepository;

    @Autowired private UserRepository userRepository;

    public String registerFarm(FarmData farmdata) {
        Farm farm = EntityMapper.toFarm(farmdata);
        // Extract userId from JWT (set by JwtFilter)
        Long userId = (Long) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        // Load the User entity
        User farmer = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Associate farm with logged-in user
        farm.setFarmer(farmer);

        farmRepository.save(farm);
        return "Farm record saved";
    }

    public FarmData getFarmsByFarmer(Long farmerId) {
        Optional<Farm> farmOptional =  farmRepository.findByFarmerUserId(farmerId);
        FarmData farmData =   farmOptional.isPresent()? EntityMapper.toFarmDTO(farmOptional.get()):new FarmData();
        return farmData;
    }



}

