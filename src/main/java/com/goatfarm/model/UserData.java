package com.goatfarm.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserData {
    private Long userId;
    private String fullName;
    @JsonProperty("passwordHash")
    private String passwordHash;
    private String email;
    private String phone;
    private Long farmId;
    private String farmName;
    // no farm here to avoid recursion
}
