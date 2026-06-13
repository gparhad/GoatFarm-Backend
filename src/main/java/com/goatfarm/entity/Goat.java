package com.goatfarm.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Goat")
@Getter
@Setter
public class Goat {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long goatId;

    @Column(name = "tag_number", nullable = false)
    private String tagNumber;
    private String breed;
    private LocalDate birthDate;
    private Double weight;
    private String healthStatus;
    private Double height;
    private Double milkPerDay;
    private Long lastKidCount;
    @Column(name = "father_tag_number")
    private String fatherTagNumber;

    @Column(name = "mother_tag_number")
    private String motherTagNumber;

    @Column(name = "gender")
    private String gender; // e.g., "Male", "Female"

    @ManyToOne
    @JoinColumn(name = "farm_id" ,referencedColumnName = "farmId")
    @JsonBackReference
    private Farm farm;

    @OneToMany(mappedBy = "goat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VaccinationRecord> vaccinationRecords = new ArrayList<>();

    @OneToMany(mappedBy = "goat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BreedingRecord> breedingRecords = new ArrayList<>();

}
