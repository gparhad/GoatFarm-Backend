package com.goatfarm.model;

import lombok.Data;

@Data
public class FarmUpdateData {
    private String farmName;
    private String location;
    private Integer size;
    private String goatType;
}
