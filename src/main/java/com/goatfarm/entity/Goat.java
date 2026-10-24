package com.goatfarm.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A goat. The tag number is the natural key inside a farm (every lookup relies on it), hence the
 * unique constraint on (farm_id, tag_number).
 */
@Entity
@Table(name = "Goat",
        uniqueConstraints = @UniqueConstraint(name = "uk_goat_farm_tag", columnNames = {"farm_id", "tag_number"}))
@Getter
@Setter
public class Goat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    /** "MALE" / "FEMALE" (see {@link com.goatfarm.domain.Gender}). */
    @Column(name = "gender")
    private String gender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id", referencedColumnName = "farmId")
    private Farm farm;

    @OneToMany(mappedBy = "goat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VaccinationRecord> vaccinationRecords = new ArrayList<>();

    @OneToMany(mappedBy = "goat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BreedingRecord> breedingRecords = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Goat other)) return false;
        return getGoatId() != null && getGoatId().equals(other.getGoatId());
    }

    @Override
    public int hashCode() {
        return Goat.class.hashCode();
    }
}
