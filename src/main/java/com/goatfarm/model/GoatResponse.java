package com.goatfarm.model;

import java.time.LocalDate;
import java.util.List;

/**
 * JSON view of a goat, field-for-field identical to what the controllers used to emit by serialising the
 * {@code Goat} entity (including the two child collections), without exposing the entity itself.
 */
public record GoatResponse(Long goatId,
                           String tagNumber,
                           String breed,
                           LocalDate birthDate,
                           Double weight,
                           String healthStatus,
                           Double height,
                           Double milkPerDay,
                           Long lastKidCount,
                           String fatherTagNumber,
                           String motherTagNumber,
                           String gender,
                           List<VaccinationEntry> vaccinationRecords,
                           List<BreedingEntry> breedingRecords) {

    public record VaccinationEntry(Long vaccinationId,
                                   String vaccineName,
                                   LocalDate vaccinationDate,
                                   String administeredBy,
                                   String dosage,
                                   String remarks,
                                   String nextVaccineName,
                                   LocalDate nextVaccinationDate,
                                   Long farmId) {
    }

    public record BreedingEntry(Long breedingId,
                                LocalDate breedingDate,
                                String pregnancyStatus,
                                Integer offspringCount,
                                String goatTagNumber,
                                String breederTagNumber,
                                LocalDate deliveryDate,
                                Integer kidsAlive,
                                Integer kidsDead,
                                LocalDate expectedKiddingDate) {
    }
}
