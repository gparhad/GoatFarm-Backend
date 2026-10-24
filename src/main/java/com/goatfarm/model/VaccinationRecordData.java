package com.goatfarm.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
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
    private Long farmId;
}
