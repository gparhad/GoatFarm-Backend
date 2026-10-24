package com.goatfarm.breeding;

import com.goatfarm.entity.BreedingRecord;
import com.goatfarm.exception.BusinessRuleViolationException;
import com.goatfarm.model.BreedingRecordData;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeliveryUpdateApplierTest {

    private final DeliveryUpdateApplier applier = new DeliveryUpdateApplier();

    private static BreedingRecordData delivered(int offspring, int alive, int dead) {
        BreedingRecordData dto = new BreedingRecordData();
        dto.setPregnancyStatus("DELIVERED");
        dto.setDeliveryDate(LocalDate.of(2026, 5, 1));
        dto.setOffspringCount(offspring);
        dto.setKidsAlive(alive);
        dto.setKidsDead(dead);
        return dto;
    }

    @Test
    void deliveredCopiesTheCounts() {
        BreedingRecord record = new BreedingRecord();

        applier.apply(record, delivered(3, 2, 1));

        assertThat(record.getPregnancyStatus()).isEqualTo("DELIVERED");
        assertThat(record.getOffspringCount()).isEqualTo(3);
        assertThat(record.getKidsAlive()).isEqualTo(2);
        assertThat(record.getKidsDead()).isEqualTo(1);
        assertThat(record.getDeliveryDate()).isEqualTo(LocalDate.of(2026, 5, 1));
    }

    @Test
    void abortedZeroesTheCountsInsteadOfBeingOverwrittenByTheRequest() {
        // Regression: the old code zeroed the counts and then overwrote them again with the request values.
        BreedingRecordData dto = delivered(3, 2, 1);
        dto.setPregnancyStatus("ABORTED");
        BreedingRecord record = new BreedingRecord();

        applier.apply(record, dto);

        assertThat(record.getOffspringCount()).isZero();
        assertThat(record.getKidsAlive()).isZero();
        assertThat(record.getKidsDead()).isZero();
        assertThat(record.getPregnancyStatus()).isEqualTo("ABORTED");
    }

    @Test
    void deliveredMustBeConsistent() {
        assertThatThrownBy(() -> applier.apply(new BreedingRecord(), delivered(3, 2, 0)))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Kids alive + dead must equal offspring count");

        BreedingRecordData noDate = delivered(3, 2, 1);
        noDate.setDeliveryDate(null);
        assertThatThrownBy(() -> applier.apply(new BreedingRecord(), noDate)).hasMessage("Delivery date is required");

        BreedingRecordData noCount = delivered(3, 2, 1);
        noCount.setOffspringCount(null);
        assertThatThrownBy(() -> applier.apply(new BreedingRecord(), noCount)).hasMessage("Offspring count is required");

        BreedingRecordData noKids = delivered(3, 2, 1);
        noKids.setKidsDead(null);
        assertThatThrownBy(() -> applier.apply(new BreedingRecord(), noKids)).hasMessage("Kids alive/dead required");
    }

    @Test
    void aMissingStatusIsRejectedInsteadOfBlankingTheRecord() {
        assertThatThrownBy(() -> applier.apply(new BreedingRecord(), new BreedingRecordData()))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Pregnancy status is required");
    }
}
