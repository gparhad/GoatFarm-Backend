package com.goatfarm.service;

import com.goatfarm.domain.VaccinationRules;
import com.goatfarm.entity.Goat;
import com.goatfarm.entity.VaccinationRecord;
import com.goatfarm.exception.BusinessRuleViolationException;
import com.goatfarm.mapper.VaccinationRecordMapper;
import com.goatfarm.model.DueVaccinationData;
import com.goatfarm.model.VaccinationRecordData;
import com.goatfarm.repository.VaccinationRecordRepository;
import com.goatfarm.support.GoatLookup;
import com.goatfarm.vaccination.VaccinationPolicy;
import com.goatfarm.vaccination.VaccineScheduleCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class VaccinationService {

    private static final Logger log = LoggerFactory.getLogger(VaccinationService.class);

    private final VaccinationRecordRepository vaccinationRecordRepository;
    private final GoatLookup goatLookup;
    private final VaccineScheduleCatalog scheduleCatalog;
    private final VaccinationPolicy policy;
    private final Clock clock;

    public VaccinationService(VaccinationRecordRepository vaccinationRecordRepository,
                              GoatLookup goatLookup,
                              VaccineScheduleCatalog scheduleCatalog,
                              VaccinationPolicy policy,
                              Clock clock) {
        this.vaccinationRecordRepository = vaccinationRecordRepository;
        this.goatLookup = goatLookup;
        this.scheduleCatalog = scheduleCatalog;
        this.policy = policy;
        this.clock = clock;
    }

    /**
     * Records a vaccination and works out when the next one is due (see {@link VaccineScheduleCatalog}:
     * every vaccine simply repeats after its own period).
     */
    @Transactional
    public VaccinationRecordData addVaccinationRecord(VaccinationRecordData vaccinationRecordData, Long farmId) {
        if (vaccinationRecordData == null
                || vaccinationRecordData.getGoatTagNumber() == null
                || vaccinationRecordData.getGoatTagNumber().isBlank()) {
            throw new BusinessRuleViolationException("Goat tag number is required");
        }
        if (vaccinationRecordData.getVaccineName() == null || vaccinationRecordData.getVaccineName().isBlank()) {
            throw new BusinessRuleViolationException("Vaccine name is required");
        }
        if (vaccinationRecordData.getVaccinationDate() == null) {
            throw new BusinessRuleViolationException("Vaccination date is required");
        }
        Goat goat = goatLookup.requireByTagNumber(vaccinationRecordData.getGoatTagNumber(), farmId);

        try {
            policy.checkMinimumGap(vaccinationRecordData.getVaccinationDate(),
                    vaccinationRecordRepository.findByGoatOrderByVaccinationDateAsc(goat));
            policy.checkDewormingAge(vaccinationRecordData.getVaccineName(), goat, vaccinationRecordData.getVaccinationDate());
        } catch (BusinessRuleViolationException ex) {
            log.warn("Vaccination rejected: goatTagNumber={}, vaccineName={}, farmId={}, reason={}",
                    goat.getTagNumber(), vaccinationRecordData.getVaccineName(), farmId, ex.getMessage());
            throw ex;
        }

        VaccinationRecord record = VaccinationRecordMapper.toEntity(vaccinationRecordData, goat);
        VaccineScheduleCatalog.NextDose next = scheduleCatalog.nextDose(record.getVaccineName(), record.getVaccinationDate());
        record.setNextVaccineName(next.vaccineName());
        record.setNextVaccinationDate(next.dueDate());

        VaccinationRecord saved = vaccinationRecordRepository.save(record);
        log.info("Vaccination recorded: vaccinationId={}, goatTagNumber={}, vaccineName={}, farmId={}, nextVaccineName={}, nextVaccinationDate={}",
                saved.getVaccinationId(), goat.getTagNumber(), saved.getVaccineName(), farmId,
                saved.getNextVaccineName(), saved.getNextVaccinationDate());
        return VaccinationRecordMapper.toDto(saved);
    }

    public List<VaccinationRecordData> getVaccinationHistoryByTagNumber(String tagNumber, Long farmId) {
        if (tagNumber == null || tagNumber.isBlank()) {
            throw new BusinessRuleViolationException("Tag number is required");
        }
        return getVaccinationHistoryByGoat(goatLookup.requireByTagNumber(tagNumber, farmId));
    }

    /** Oldest first. */
    public List<VaccinationRecordData> getVaccinationHistoryByGoat(Goat goat) {
        return vaccinationRecordRepository.findByGoatOrderByVaccinationDateAsc(goat).stream()
                .map(VaccinationRecordMapper::toDto)
                .toList();
    }

    /** Vaccinations due within the next {@code days} days; an empty list for invalid input (existing behaviour). */
    public List<VaccinationRecordData> getUpcomingVaccinations(Long farmId, int days) {
        if (farmId == null || days <= 0) {
            return List.of();
        }
        LocalDate from = LocalDate.now(clock);
        List<VaccinationRecordData> upcoming =
                vaccinationRecordRepository.findUpcomingVaccinations(farmId, from, from.plusDays(days)).stream()
                        .map(VaccinationRecordMapper::toDto)
                        .toList();
        log.debug("Upcoming vaccinations: farmId={}, days={}, count={}", farmId, days, upcoming.size());
        return upcoming;
    }

    /**
     * Vaccinations falling due exactly {@link VaccinationRules#DUE_SOON_OFFSET_DAYS} days from today.
     * Served by {@code GET /api/vaccinations/schedule/tomorrow}: the URL says "tomorrow" but the behaviour has
     * always been +7 days, and it is kept so existing clients do not change.
     */
    public List<DueVaccinationData> getVaccinationsDueSoon(Long farmId) {
        LocalDate dueDate = LocalDate.now(clock).plusDays(VaccinationRules.DUE_SOON_OFFSET_DAYS);
        List<DueVaccinationData> due = vaccinationRecordRepository.findDueVaccinationsByFarmAndDate(farmId, dueDate).stream()
                .map(record -> new DueVaccinationData(
                        record.getGoat().getTagNumber(),
                        record.getNextVaccineName(),
                        record.getVaccinationDate(),
                        record.getNextVaccinationDate()))
                .toList();
        log.debug("Vaccinations due soon: farmId={}, dueDate={}, count={}", farmId, dueDate, due.size());
        return due;
    }
}
