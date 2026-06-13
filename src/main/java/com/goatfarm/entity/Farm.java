package com.goatfarm.entity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "Farm")
@Getter
@Setter
public class Farm {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long farmId;

    private String farmName;
    private String location;
    private Double size;
    private String goatTypes;

    @OneToOne
    @JoinColumn(name = "farmer_id", referencedColumnName = "userId", unique = true)
    @JsonIgnore
    private User farmer;

    @OneToMany(mappedBy = "farm", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Goat> goats;
}

