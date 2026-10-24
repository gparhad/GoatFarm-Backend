package com.goatfarm.model;

import java.time.LocalDate;

/** One row of the "vaccinations due soon" schedule. */
public record DueVaccinationData(String goatTagNumber,
                                 String vaccineName,
                                 LocalDate lastDate,
                                 LocalDate nextVaccinationDate) {
}
