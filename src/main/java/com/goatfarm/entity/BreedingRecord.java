package com.goatfarm.entity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "BreedingRecords")
@Data
public class BreedingRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long breedingId;

    private LocalDate breedingDate;
    private String pregnancyStatus;
    private Integer offspringCount;
    private String goatTagNumber;
    private String breederTagNumber;

    private LocalDate expected_kidding_date;

    @ManyToOne
    @JoinColumn(name = "goat_id")
    private Goat goat;

    @ManyToOne
    @JoinColumn(name = "mate_id")
    private Goat mate;
}

