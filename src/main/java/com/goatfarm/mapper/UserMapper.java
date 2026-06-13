package com.goatfarm.mapper;

import com.goatfarm.entity.User;
import com.goatfarm.model.UserData;

import java.util.Objects;

public class UserMapper {
    // User → UserDTO
    public static UserData toUserDTO(User user, boolean passwordRequired) {
        if (user == null) return null;
        UserData dto = new UserData();
        dto.setUserId(user.getUserId());
        dto.setUserName(user.getUserName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        if(Objects.nonNull(user.getFarm()))
        {
            dto.setFarmId(user.getFarm().getFarmId());
            dto.setFarmName(user.getFarm().getFarmName());
        }
        if(passwordRequired)
        {
            dto.setPasswordHash(user.getPasswordHash());
        }
        return dto;
    }

    // UserDTO → User
    public static User toUser(UserData dto, boolean passwordRequired) {
        if (dto == null) return null;
        User user = new User();
        user.setUserId(dto.getUserId());
        user.setUserName(dto.getUserName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        if(passwordRequired)
        {
            dto.setPasswordHash(user.getPasswordHash());
        }        return user;
    }

    public static User toUserForUpdate(UserData userData, Long userId){
        if(userData == null) return null;
        User user = new User();
        user.setUserId(userId);
        user.setEmail(userData.getEmail());
        user.setPhone(userData.getPhone());
        user.setFullName(user.getFullName());
        return user;
    }

}
