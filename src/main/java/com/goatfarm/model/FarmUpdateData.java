package com.goatfarm.model;

import lombok.Data;

@Data
public class FarmUpdateData {
    private String farmName;
    private String location;
    private Double size;
    private String goatType;
}
