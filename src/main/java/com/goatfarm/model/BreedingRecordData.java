package com.goatfarm.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BreedingRecordData {
    private Long breedingId;
    private LocalDate breedingDate;
    private String pregnancyStatus;
    private Integer offspringCount;
    private String goatTagNumber;
    private String breederTagNumber;
    private LocalDate expectedKiddingDate;
    private Long goatId;
    private Long mateId;
}
