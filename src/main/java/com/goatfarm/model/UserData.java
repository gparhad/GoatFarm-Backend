package com.goatfarm.model;

import com.goatfarm.entity.Farm;
import lombok.Data;

@Data
public class UserData {
    private Long userId;
    private String fullName;
    private String userName;
    private String passwordHash;
    private String email;
    private String phone;
    private Long farmId;
    private String farmName;
    // no farm here to avoid recursion
}
