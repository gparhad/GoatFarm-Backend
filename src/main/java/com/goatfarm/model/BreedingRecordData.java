package com.goatfarm.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BreedingRecordData {
    private Long breedingId;
    private LocalDate breedingDate;
    private String pregnancyStatus;
    private Integer offspringCount;

    @NotBlank(message = "goatTagNumber is required")
    private String goatTagNumber;

    @NotBlank(message = "breederTagNumber is required")
    private String breederTagNumber;

    private LocalDate expectedKiddingDate;
    private Long goatId;
    private Long mateId;
    private LocalDate deliveryDate;
    private Integer kidsAlive;
    private Integer kidsDead;
}
