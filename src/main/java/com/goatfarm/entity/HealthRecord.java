package com.goatfarm.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "HealthRecords")
@Data
public class HealthRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recordId;

    private LocalDate checkupDate;
    private String diagnosis;
    private String treatment;
    private String vaccination;

    @ManyToOne
    @JoinColumn(name = "goat_id")
    private Goat goat;
}

