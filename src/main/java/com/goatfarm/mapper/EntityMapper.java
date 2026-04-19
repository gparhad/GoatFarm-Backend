package com.goatfarm.mapper;

import com.goatfarm.entity.Farm;
import com.goatfarm.entity.User;
import com.goatfarm.model.FarmData;
import com.goatfarm.model.UserData;

import java.util.Objects;

public class EntityMapper {

    // User → UserDTO
    public static UserData toUserDTO(User user) {
        if (user == null) return null;
        UserData dto = new UserData();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setPasswordHash(user.getPasswordHash());
        if(Objects.nonNull(user.getFarm()))
        {
            dto.setFarmId(user.getFarm().getFarmId());
        }
        return dto;
    }

    // UserDTO → User
    public static User toUser(UserData dto) {
        if (dto == null) return null;
        User user = new User();
        user.setUserId(dto.getUserId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setPasswordHash(dto.getPasswordHash());
        return user;
    }

    // Farm → FarmDTO
    public static FarmData toFarmDTO(Farm farm) {
        if (farm == null) return null;
        FarmData dto = new FarmData();
        dto.setFarmId(farm.getFarmId());
        dto.setFarmName(farm.getFarmName());
        dto.setLocation(farm.getLocation());
        dto.setSize(farm.getSize());
        dto.setGoatTypes(farm.getGoatTypes());
        dto.setFarmer(toUserDTO(farm.getFarmer())); // limited user info
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
        farm.setFarmer(toUser(dto.getFarmer()));
        return farm;
    }
}

