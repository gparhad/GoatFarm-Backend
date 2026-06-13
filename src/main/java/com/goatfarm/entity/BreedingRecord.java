package com.goatfarm.entity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "Breeding_Records")
@Data
public class BreedingRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long breedingId;

    private LocalDate breedingDate;
    private String pregnancyStatus;
    private Integer offspringCount;
    private String goatTagNumber;
    private String breederTagNumber;

    private LocalDate deliveryDate;
    private Integer kidsAlive;
    private Integer kidsDead;

    private LocalDate expectedKiddingDate;

    @ManyToOne
    @JoinColumn(name = "goat_id")
    @JsonBackReference
    private Goat goat;

    @ManyToOne
    @JoinColumn(name = "mate_id")
    @JsonBackReference
    private Goat mate;

    @ManyToOne
    @JoinColumn(name = "farm_id", referencedColumnName = "farmId")
    @JsonBackReference
    private Farm farm;
}

