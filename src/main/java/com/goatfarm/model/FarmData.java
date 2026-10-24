package com.goatfarm.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FarmData {
    private Long farmId;
    private String farmName;
    private String location;
    private Double size;
    private String goatTypes;
    private Long userId;
    /** Limited user info (never includes credentials). */
    private UserData farmer;
}
