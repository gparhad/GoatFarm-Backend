package com.goatfarm.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "vaccination_record")
@Data
public class VaccinationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vaccinationId;

    @ManyToOne
    @JoinColumn(name = "goat_id", nullable = false)
    private Goat goat;

    private String vaccineName;
    private LocalDate vaccinationDate;
    private String administeredBy;
    private String dosage;
    private String remarks;

    private String nextVaccineName;
    private LocalDate nextVaccinationDate;


    // getters and setters
}

