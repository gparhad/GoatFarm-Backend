package com.goatfarm.model;

import com.goatfarm.entity.Farm;
import lombok.Data;

@Data
public class UserData {
    private Long userId;
    private String username;
    private String passwordHash;
    private String email;
    private String phone;
    private Long farmId;
    // no farm here to avoid recursion
}
