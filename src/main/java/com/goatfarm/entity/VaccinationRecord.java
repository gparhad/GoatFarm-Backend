package com.goatfarm.entity;

import jakarta.persistence.Column;
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
@Table(name = "vaccination_record")
@Getter
@Setter
public class VaccinationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vaccinationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goat_id", nullable = false)
    private Goat goat;

    private String vaccineName;
    private LocalDate vaccinationDate;
    private String administeredBy;
    private String dosage;
    private String remarks;
    private String nextVaccineName;
    private LocalDate nextVaccinationDate;

    /** Denormalised copy of the goat's farm id, kept for existing rows/consumers; queries scope through the goat. */
    @Column(name = "farm_id", nullable = true)
    private Long farmId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VaccinationRecord other)) return false;
        return getVaccinationId() != null && getVaccinationId().equals(other.getVaccinationId());
    }

    @Override
    public int hashCode() {
        return VaccinationRecord.class.hashCode();
    }
}
