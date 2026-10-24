package com.goatfarm.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserData {
    private Long userId;
    private String fullName;

    /** Accepted on input (registration sends the plain password in this field); never written to responses. */
    @JsonProperty(value = "passwordHash", access = JsonProperty.Access.WRITE_ONLY)
    @ToString.Exclude
    private String passwordHash;

    private String email;
    private String phone;
    private Long farmId;
    private String farmName;
}
