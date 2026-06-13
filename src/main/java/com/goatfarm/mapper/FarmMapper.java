package com.goatfarm.mapper;

import com.goatfarm.entity.Farm;
import com.goatfarm.entity.User;
import com.goatfarm.model.FarmData;
import com.goatfarm.model.UserData;

import java.util.Objects;

import static com.goatfarm.mapper.UserMapper.toUser;
import static com.goatfarm.mapper.UserMapper.toUserDTO;

public class FarmMapper {
    // Farm → FarmDTO
    public static FarmData toFarmDTO(Farm farm) {
        if (farm == null) return null;
        FarmData dto = new FarmData();
        dto.setFarmId(farm.getFarmId());
        dto.setFarmName(farm.getFarmName());
        dto.setLocation(farm.getLocation());
        dto.setSize(farm.getSize());
        dto.setGoatTypes(farm.getGoatTypes());
        dto.setFarmer(toUserDTO(farm.getFarmer(), false)); // limited user info
        return dto;
    }

    // FarmDTO → Farm
    public static Farm toFarm(FarmData dto) {
        if (dto == null) return null;
        Farm farm = new Farm();
        farm.setFarmId(dto.getFarmId());
        farm.setFarmName(dto.getFarmName());
        farm.setLocation(dto.getLocation());
        farm.setSize(dto.getSize());
        farm.setGoatTypes(dto.getGoatTypes());
        farm.setFarmer(toUser(dto.getFarmer(), false));
        return farm;
    }
}

