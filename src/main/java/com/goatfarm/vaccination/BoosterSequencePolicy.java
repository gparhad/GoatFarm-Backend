package com.goatfarm.vaccination;

import com.goatfarm.entity.VaccinationRecord;
import com.goatfarm.exception.BusinessRuleViolationException;
import com.goatfarm.model.VaccinationRecordData;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Booster-sequence rules ("after FMD only FMD Booster is allowed", minimum interval after a booster).
 *
 * <p><b>Not enforced.</b> These rules existed in the original code but every call to them was commented
 * out, so they never ran. They are preserved here, isolated and unit-testable, so enabling them is a
 * one-line change in {@code VaccinationService} once the business confirms the rules.
 */
public final class BoosterSequencePolicy {

    private static final Set<String> REQUIRE_BOOSTER =
            Set.of("FMD", "Goat Pox", "Enterotoxemia", "Hemorrhagic Septicemia");

    private BoosterSequencePolicy() {
    }

    public static void validate(List<VaccinationRecord> records, VaccinationRecordData dto) {
        Optional<VaccinationRecord> last = records.stream()
                .filter(r -> r.getVaccineName() != null && r.getVaccinationDate() != null)
                .filter(r -> r.getVaccineName().equalsIgnoreCase(dto.getVaccineName()))
                .max(Comparator.comparing(VaccinationRecord::getVaccinationDate));
        if (last.isEmpty()) {
            return;
        }
        VaccinationRecord lastRecord = last.get();
        boolean lastWasBooster = isBooster(lastRecord.getVaccineName());
        boolean currentIsBooster = isBooster(dto.getVaccineName());

        if (!lastWasBooster && REQUIRE_BOOSTER.contains(lastRecord.getVaccineName())) {
            if (!currentIsBooster || !startsWithIgnoreCase(lastRecord.getVaccineName(), dto.getVaccineName())) {
                throw new BusinessRuleViolationException("Invalid vaccination: After " + lastRecord.getVaccineName()
                        + " only its booster is allowed next.");
            }
        }
        if (lastWasBooster) {
            enforceMinimumIntervalAfterBooster(lastRecord, dto);
        }
    }

    private static void enforceMinimumIntervalAfterBooster(VaccinationRecord lastRecord, VaccinationRecordData dto) {
        LocalDate date = dto.getVaccinationDate();
        LocalDate lastDate = lastRecord.getVaccinationDate();
        if (startsWithIgnoreCase(lastRecord.getVaccineName(), "FMD") && date.isBefore(lastDate.plusMonths(6))) {
            throw new BusinessRuleViolationException(
                    "Invalid vaccination: After FMD booster, next FMD dose cannot be given before 6 months.");
        }
        boolean yearlyVaccine = startsWithIgnoreCase(lastRecord.getVaccineName(), "Goat Pox")
                || startsWithIgnoreCase(lastRecord.getVaccineName(), "Enterotoxemia")
                || startsWithIgnoreCase(lastRecord.getVaccineName(), "Hemorrhagic Septicemia");
        if (yearlyVaccine && date.isBefore(lastDate.plusYears(1))) {
            throw new BusinessRuleViolationException("Invalid vaccination: After " + dto.getVaccineName()
                    + " booster, next dose cannot be given before 12 months.");
        }
    }

    private static boolean isBooster(String vaccineName) {
        return vaccineName != null && vaccineName.toLowerCase().contains("booster");
    }

    private static boolean startsWithIgnoreCase(String value, String prefix) {
        return value != null && prefix != null && value.toLowerCase().startsWith(prefix.toLowerCase());
    }
}
