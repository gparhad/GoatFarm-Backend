package com.goatfarm.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class GoatData {
    private Long goatId;

    @NotBlank(message = "tagNumber is required")
    private String tagNumber;

    private String breed;
    private LocalDate birthDate;
    private Double weight;
    private String healthStatus;
    private String fatherTagNumber;
    private String motherTagNumber;
    private String gender;
    private Double height;
    private Double milkPerDay;
    private Long lastKidCount;

    /** Only filled by the endpoints that return a goat together with its records (add, list, update). */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<VaccinationRecordData> vaccinationRecords;

    /** Only filled by the endpoints that return a goat together with its records (add, list, update). */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<BreedingRecordData> breedingRecords;
}
