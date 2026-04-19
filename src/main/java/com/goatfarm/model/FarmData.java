package com.goatfarm.model;

import lombok.Data;

@Data
public class FarmData {
    private Long farmId;
    private String farmName;
    private String location;
    private Double size;
    private String goatTypes;
    private UserData farmer; // include limited user info
}

