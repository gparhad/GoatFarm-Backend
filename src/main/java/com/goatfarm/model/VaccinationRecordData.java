package com.goatfarm.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class VaccinationRecordData {
    private Long vaccinationId;
    private Long goatId;
    private String goatTagNumber;

    private String vaccineName;
    private LocalDate vaccinationDate;
    private String administeredBy;
    private String dosage;
    private String remarks;

    private String nextVaccineName;
    private LocalDate nextVaccinationDate;

    // getters and setters
}
