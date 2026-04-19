package com.goatfarm.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class GoatData {
    private Long goatId;
    private String tagNumber;
    private String breed;
    private LocalDate birthDate;
    private Double weight;
    private String healthStatus;
    private String fatherTagNumber;
    private String motherTagNumber;
    private String gender;
}
