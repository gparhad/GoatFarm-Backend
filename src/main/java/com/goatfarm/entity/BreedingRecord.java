package com.goatfarm.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "Breeding_Records")
@Getter
@Setter
public class BreedingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goat_id")
    private Goat goat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mate_id")
    private Goat mate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id", referencedColumnName = "farmId")
    private Farm farm;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BreedingRecord other)) return false;
        return getBreedingId() != null && getBreedingId().equals(other.getBreedingId());
    }

    @Override
    public int hashCode() {
        return BreedingRecord.class.hashCode();
    }
}
