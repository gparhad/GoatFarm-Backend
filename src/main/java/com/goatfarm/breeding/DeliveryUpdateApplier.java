package com.goatfarm.breeding;

import com.goatfarm.domain.PregnancyStatus;
import com.goatfarm.entity.BreedingRecord;
import com.goatfarm.exception.BusinessRuleViolationException;
import com.goatfarm.model.BreedingRecordData;
import org.springframework.stereotype.Component;

/** Applies the outcome of a pregnancy (delivered, aborted ...) to a breeding record. */
@Component
public class DeliveryUpdateApplier {

    public void apply(BreedingRecord record, BreedingRecordData dto) {
        String status = dto.getPregnancyStatus();
        if (status == null || status.isBlank()) {
            throw new BusinessRuleViolationException("Pregnancy status is required");
        }
        if (PregnancyStatus.DELIVERED.matches(status)) {
            requireValidDelivery(dto);
        }

        record.setPregnancyStatus(status);
        record.setDeliveryDate(dto.getDeliveryDate());

        if (PregnancyStatus.ABORTED.matches(status)) {
            record.setOffspringCount(0);
            record.setKidsAlive(0);
            record.setKidsDead(0);
            return;
        }
        record.setOffspringCount(dto.getOffspringCount());
        record.setKidsAlive(dto.getKidsAlive());
        record.setKidsDead(dto.getKidsDead());
    }

    private static void requireValidDelivery(BreedingRecordData dto) {
        if (dto.getDeliveryDate() == null) {
            throw new BusinessRuleViolationException("Delivery date is required");
        }
        if (dto.getOffspringCount() == null) {
            throw new BusinessRuleViolationException("Offspring count is required");
        }
        if (dto.getKidsAlive() == null || dto.getKidsDead() == null) {
            throw new BusinessRuleViolationException("Kids alive/dead required");
        }
        int offspring = dto.getOffspringCount();
        if (offspring != dto.getKidsAlive() + dto.getKidsDead()) {
            throw new BusinessRuleViolationException("Kids alive + dead must equal offspring count");
        }
    }
}
