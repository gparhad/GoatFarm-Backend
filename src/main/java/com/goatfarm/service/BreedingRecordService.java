package com.goatfarm.service;

import com.goatfarm.breeding.DeliveryUpdateApplier;
import com.goatfarm.domain.BreedingRules;
import com.goatfarm.domain.PregnancyStatus;
import com.goatfarm.entity.BreedingRecord;
import com.goatfarm.entity.Goat;
import com.goatfarm.exception.BusinessRuleViolationException;
import com.goatfarm.exception.ResourceNotFoundException;
import com.goatfarm.mapper.BreedingRecordMapper;
import com.goatfarm.model.ApiResult;
import com.goatfarm.model.BreedingRecordData;
import com.goatfarm.model.InbreedingCheckResponse;
import com.goatfarm.pedigree.InbreedingEvaluator;
import com.goatfarm.pedigree.PedigreeService;
import com.goatfarm.repository.BreedingRecordRepository;
import com.goatfarm.repository.GoatRepository;
import com.goatfarm.support.GoatLookup;
import com.goatfarm.support.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class BreedingRecordService {

    private static final Logger log = LoggerFactory.getLogger(BreedingRecordService.class);

    private final BreedingRecordRepository breedingRecordRepository;
    private final GoatRepository goatRepository;
    private final GoatLookup goatLookup;
    private final PedigreeService pedigreeService;
    private final InbreedingEvaluator inbreedingEvaluator;
    private final DeliveryUpdateApplier deliveryUpdateApplier;
    private final Clock clock;

    public BreedingRecordService(BreedingRecordRepository breedingRecordRepository,
                                 GoatRepository goatRepository,
                                 GoatLookup goatLookup,
                                 PedigreeService pedigreeService,
                                 InbreedingEvaluator inbreedingEvaluator,
                                 DeliveryUpdateApplier deliveryUpdateApplier,
                                 Clock clock) {
        this.breedingRecordRepository = breedingRecordRepository;
        this.goatRepository = goatRepository;
        this.goatLookup = goatLookup;
        this.pedigreeService = pedigreeService;
        this.inbreedingEvaluator = inbreedingEvaluator;
        this.deliveryUpdateApplier = deliveryUpdateApplier;
        this.clock = clock;
    }

    /**
     * Records a breeding. The goat row is locked for the duration of the transaction, so two simultaneous
     * requests cannot both pass the "no active pregnancy" check.
     */
    @Transactional
    public BreedingRecordData addBreedingRecord(BreedingRecordData breedingRecordData, Long farmId) {
        String goatTagNumber = breedingRecordData.getGoatTagNumber();
        Goat goat = goatRepository.findForUpdateByTagNumberAndFarmId(goatTagNumber, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Goat not found"));
        if (breedingRecordRepository.existsByGoatTagNumberAndPregnancyStatusAndFarm_FarmId(
                goatTagNumber, PregnancyStatus.PREGNANT.name(), farmId)) {
            log.warn("Breeding rejected: goat already pregnant, goatTagNumber={}, farmId={}", goatTagNumber, farmId);
            throw new BusinessRuleViolationException("Goat already has an active pregnancy. Cannot add new breeding record.");
        }
        Goat breeder = goatLookup.require(breedingRecordData.getBreederTagNumber(), farmId, "Breeder not found");

        InbreedingCheckResponse relation = inbreedingEvaluator.evaluate(goat, breeder, pedigreeService.snapshot(farmId));
        if (relation.isInbreeding()) {
            log.warn("Breeding rejected: inbreeding, goatTagNumber={}, breederTagNumber={}, farmId={}, reason={}",
                    goatTagNumber, breeder.getTagNumber(), farmId, relation.message());
            throw new BusinessRuleViolationException(relation.message());
        }

        BreedingRecord record = BreedingRecordMapper.toEntity(breedingRecordData, goat, breeder);
        if (record.getExpectedKiddingDate() == null && record.getBreedingDate() != null) {
            record.setExpectedKiddingDate(record.getBreedingDate().plusDays(BreedingRules.GESTATION_DAYS));
        }
        BreedingRecord saved = breedingRecordRepository.save(record);
        log.info("Breeding recorded: breedingId={}, goatTagNumber={}, breederTagNumber={}, farmId={}, expectedKiddingDate={}",
                saved.getBreedingId(), goatTagNumber, breeder.getTagNumber(), farmId, saved.getExpectedKiddingDate());
        return BreedingRecordMapper.toDto(saved);
    }

    public List<BreedingRecordData> getBreedingRecordsByGoatTagNumber(String goatTagNumber, Long farmId) {
        if (farmId == null) {
            return List.of();
        }
        return breedingRecordRepository.findByGoatTagNumberAndFarm_FarmId(goatTagNumber, farmId).stream()
                .map(BreedingRecordMapper::toDto)
                .toList();
    }

    public List<BreedingRecordData> getBreedingRecordsByBreederTagNumber(String breederTagNumber, Long farmId) {
        if (farmId == null) {
            return List.of();
        }
        return breedingRecordRepository.findByBreederTagNumberAndFarm_FarmId(breederTagNumber, farmId).stream()
                .map(BreedingRecordMapper::toDto)
                .toList();
    }

    /** Partial update: only fields present in the request change. The record and any referenced goats must belong to the caller's farm. */
    @Transactional
    public BreedingRecordData updateBreedingRecord(Long breedingId, BreedingRecordData breedingRecordData, Long farmId) {
        BreedingRecord record = breedingRecordRepository.findByBreedingIdAndFarm_FarmId(breedingId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Breeding record not found with id: " + breedingId));
        applyUpdate(record, breedingRecordData, farmId);
        BreedingRecord saved = breedingRecordRepository.save(record);
        log.info("Breeding record updated: breedingId={}, farmId={}", breedingId, farmId);
        return BreedingRecordMapper.toDto(saved);
    }

    private void applyUpdate(BreedingRecord record, BreedingRecordData update, Long farmId) {
        Values.ifNotNull(update.getBreedingDate(), date -> {
            record.setBreedingDate(date);
            record.setExpectedKiddingDate(date.plusDays(BreedingRules.GESTATION_DAYS));
        });
        Values.ifNotNull(update.getPregnancyStatus(), record::setPregnancyStatus);
        Values.ifNotNull(update.getOffspringCount(), record::setOffspringCount);
        Values.ifNotNull(update.getKidsAlive(), record::setKidsAlive);
        Values.ifNotNull(update.getKidsDead(), record::setKidsDead);

        if (update.getGoatId() != null) {
            Goat goat = goatRepository.findByGoatIdAndFarm_FarmId(update.getGoatId(), farmId)
                    .orElseThrow(() -> new ResourceNotFoundException("Goat not found"));
            record.setGoat(goat);
            record.setGoatTagNumber(goat.getTagNumber());
        }
        if (update.getMateId() != null) {
            Goat mate = goatRepository.findByGoatIdAndFarm_FarmId(update.getMateId(), farmId)
                    .orElseThrow(() -> new ResourceNotFoundException("Mate not found"));
            record.setMate(mate);
            record.setBreederTagNumber(mate.getTagNumber());
        }
    }

    public ApiResult<BreedingRecordData> getLatestPregnantBreedingRecord(String goatTagNumber, Long farmId) {
        BreedingRecord record = findActivePregnancy(goatTagNumber, farmId);
        return ApiResult.ok("Active breeding record found", BreedingRecordMapper.toDto(record));
    }

    @Transactional
    public ApiResult<BreedingRecordData> updateDeliveryDetails(String goatTagNumber,
                                                               BreedingRecordData breedingRecordData,
                                                               Long farmId) {
        BreedingRecord record = findActivePregnancy(goatTagNumber, farmId);
        deliveryUpdateApplier.apply(record, breedingRecordData);
        BreedingRecord saved = breedingRecordRepository.save(record);
        log.info("Delivery details updated: breedingId={}, goatTagNumber={}, farmId={}, status={}, kidsAlive={}, kidsDead={}",
                saved.getBreedingId(), goatTagNumber, farmId, saved.getPregnancyStatus(), saved.getKidsAlive(), saved.getKidsDead());
        return ApiResult.ok("Breeding record updated successfully", BreedingRecordMapper.toDto(saved));
    }

    public List<BreedingRecordData> getUpcomingDeliveries(Long farmId, int pastDays, int futureDays) {
        if (farmId == null) {
            throw new BusinessRuleViolationException("FarmId must not be null");
        }
        if (pastDays < 0) {
            throw new BusinessRuleViolationException("pastDays must be >= 0");
        }
        if (futureDays < 0) {
            throw new BusinessRuleViolationException("futureDays must be >= 0");
        }
        LocalDate today = LocalDate.now(clock);
        List<BreedingRecordData> deliveries = breedingRecordRepository
                .findDueDeliveriesInWindow(farmId, today.minusDays(pastDays), today.plusDays(futureDays),
                        PregnancyStatus.PREGNANT.name())
                .stream()
                .map(BreedingRecordMapper::toDto)
                .toList();
        log.debug("Upcoming deliveries: farmId={}, window=-{}/+{} days, count={}", farmId, pastDays, futureDays, deliveries.size());
        return deliveries;
    }

    private BreedingRecord findActivePregnancy(String goatTagNumber, Long farmId) {
        return breedingRecordRepository
                .findFirstByGoatTagNumberAndPregnancyStatusAndFarm_FarmIdOrderByBreedingIdDesc(
                        goatTagNumber, PregnancyStatus.PREGNANT.name(), farmId)
                .orElseThrow(() -> new ResourceNotFoundException("No active pregnancy found for the goat"));
    }
}
