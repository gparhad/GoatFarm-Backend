package com.goatfarm.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
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

//    public UpcomingDeliveryDateData(Long breedingId, String goatTagNumber, String breederTagNumber, String pregnancyStatus, LocalDate expectedKiddingDate, LocalDate deliveryDate, Integer kidsAlive, Integer kidsDead, Integer offspringCount, LocalDate breedingDate) {
//
//    }
}
