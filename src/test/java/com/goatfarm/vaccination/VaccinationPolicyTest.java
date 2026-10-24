package com.goatfarm.vaccination;

import com.goatfarm.entity.Goat;
import com.goatfarm.entity.VaccinationRecord;
import com.goatfarm.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VaccinationPolicyTest {

    private final VaccinationPolicy policy = new VaccinationPolicy();

    private static VaccinationRecord recordOn(LocalDate date) {
        VaccinationRecord record = new VaccinationRecord();
        record.setVaccinationDate(date);
        return record;
    }

    @Test
    void rejectsADoseWithin21DaysOfTheLatestOne() {
        List<VaccinationRecord> history = List.of(recordOn(LocalDate.of(2026, 3, 1)));

        assertThatThrownBy(() -> policy.checkMinimumGap(LocalDate.of(2026, 3, 21), history))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Invalid vaccination: No two vaccines/deworming should be given within 21 days. "
                        + "Last record was on 2026-03-01");
    }

    @Test
    void acceptsExactly21DaysAfterOrAnEmptyHistory() {
        List<VaccinationRecord> history = List.of(recordOn(LocalDate.of(2026, 3, 1)));

        assertThatCode(() -> policy.checkMinimumGap(LocalDate.of(2026, 3, 22), history)).doesNotThrowAnyException();
        assertThatCode(() -> policy.checkMinimumGap(LocalDate.of(2026, 3, 2), List.of())).doesNotThrowAnyException();
    }

    @Test
    void usesTheNewestRecordNotTheListOrder() {
        List<VaccinationRecord> history = List.of(recordOn(LocalDate.of(2026, 3, 1)), recordOn(LocalDate.of(2026, 1, 1)));

        assertThatThrownBy(() -> policy.checkMinimumGap(LocalDate.of(2026, 3, 10), history))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void dewormingNeedsAtLeastThreeMonthsOfAge() {
        Goat kid = new Goat();
        kid.setBirthDate(LocalDate.of(2026, 1, 10));

        assertThatThrownBy(() -> policy.checkDewormingAge("Deworming", kid, LocalDate.of(2026, 4, 9)))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Deworming cannot be given before 3 months of age.");
        assertThatCode(() -> policy.checkDewormingAge("Deworming", kid, LocalDate.of(2026, 4, 10))).doesNotThrowAnyException();
        assertThatCode(() -> policy.checkDewormingAge("FMD", kid, LocalDate.of(2026, 1, 11))).doesNotThrowAnyException();
    }
}
