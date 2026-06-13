package com.goatfarm.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
    @JsonBackReference
    private Goat goat;

    private String vaccineName;
    private LocalDate vaccinationDate;
    private String administeredBy;
    private String dosage;
    private String remarks;

    private String nextVaccineName;
    private LocalDate nextVaccinationDate;

    @Column(name = "farm_id", nullable = true)
    private Long farmId;


    // getters and setters
}

