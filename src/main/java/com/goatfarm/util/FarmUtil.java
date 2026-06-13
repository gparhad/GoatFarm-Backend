package com.goatfarm.util;

import com.goatfarm.entity.Farm;
import com.goatfarm.repository.FarmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class FarmUtil {
    @Autowired
    public FarmRepository farmRepository;

    public Farm getFarmByFarmerId(){
        Long userId = (Long) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

       return farmRepository.findByFarmerUserId(userId).get();

    }
}
