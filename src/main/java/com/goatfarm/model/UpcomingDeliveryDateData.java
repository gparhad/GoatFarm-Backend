package com.goatfarm.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpcomingDeliveryDateData {
    private Long breedingId;
    private LocalDate breedingDate;
    private String pregnancyStatus;
    private Integer offspringCount;
    private String goatTagNumber;
    private String breederTagNumber;
    private LocalDate expectedKiddingDate;
    private LocalDate deliveryDate;
    private Integer kidsAlive;
    private Integer kidsDead;
}
