package com.goatfarm.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "Farm")
@Getter
@Setter
public class Farm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long farmId;

    private String farmName;
    private String location;
    private Double size;
    private String goatTypes;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_id", referencedColumnName = "userId", unique = true)
    private User farmer;

    @OneToMany(mappedBy = "farm", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Goat> goats;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Farm other)) return false;
        return getFarmId() != null && getFarmId().equals(other.getFarmId());
    }

    @Override
    public int hashCode() {
        return Farm.class.hashCode();
    }
}
