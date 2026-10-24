package com.goatfarm.mapper;

import com.goatfarm.entity.Farm;
import com.goatfarm.entity.User;
import com.goatfarm.model.UserData;

public final class UserMapper {

    private UserMapper() {
    }

    /** Never exposes the password (the DTO field is write-only as well). */
    public static UserData toDto(User user,boolean passwordRequired) {
        if (user == null) {
            return null;
        }
        UserData dto = new UserData();
        dto.setUserId(user.getUserId());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setFullName(user.getFullName());
        Farm farm = user.getFarm();
        if (farm != null) {
            dto.setFarmId(farm.getFarmId());
            dto.setFarmName(farm.getFarmName());
        }
        if(passwordRequired)
        {
            dto.setPasswordHash(user.getPasswordHash());
        }
        return dto;
    }

    /** @param email already normalised; @param encodedPassword already hashed */
    public static User toEntity(UserData dto, String email, String encodedPassword) {
        User user = new User();
        user.setFullName(dto.getFullName());
        user.setEmail(email);
        user.setPhone(dto.getPhone());
        user.setPasswordHash(encodedPassword);
        return user;
    }

    /** Email and phone follow the request (null clears them, as before); the name is only changed when one is supplied. */
    public static void applyUpdate(User user, UserData data, String email) {
        user.setEmail(email);
        user.setPhone(data.getPhone());
        if (data.getFullName() != null && !data.getFullName().isBlank()) {
            user.setFullName(data.getFullName());
        }
    }
}
