package com.goatfarm.vaccination;

import com.goatfarm.domain.VaccinationRules;
import com.goatfarm.entity.Goat;
import com.goatfarm.entity.VaccinationRecord;
import com.goatfarm.exception.BusinessRuleViolationException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Validation rules applied before a vaccination record is accepted (messages are part of the API contract). */
@Component
public class VaccinationPolicy {

    public static final String DEWORMING = "Deworming";

    /** No two vaccines/dewormings within {@link VaccinationRules#MIN_DAYS_BETWEEN_DOSES} days of the latest one. */
    public void checkMinimumGap(LocalDate newDate, List<VaccinationRecord> existing) {
        Optional<VaccinationRecord> latest = existing.stream()
                .filter(record -> record.getVaccinationDate() != null)
                .max(Comparator.comparing(VaccinationRecord::getVaccinationDate));
        if (latest.isPresent()) {
            LocalDate lastDate = latest.get().getVaccinationDate();
            if (newDate.isBefore(lastDate.plusDays(VaccinationRules.MIN_DAYS_BETWEEN_DOSES))) {
                throw new BusinessRuleViolationException(
                        "Invalid vaccination: No two vaccines/deworming should be given within "
                                + VaccinationRules.MIN_DAYS_BETWEEN_DOSES + " days. "
                                + "Last record was on " + lastDate);
            }
        }
    }

    public void checkDewormingAge(String vaccineName, Goat goat, LocalDate givenOn) {
        if (DEWORMING.equals(vaccineName)
                && goat.getBirthDate() != null
                && goat.getBirthDate().plusMonths(VaccinationRules.DEWORMING_MIN_AGE_MONTHS).isAfter(givenOn)) {
            throw new BusinessRuleViolationException("Deworming cannot be given before "
                    + VaccinationRules.DEWORMING_MIN_AGE_MONTHS + " months of age.");
        }
    }
}
