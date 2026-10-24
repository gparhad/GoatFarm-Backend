package com.goatfarm.service;

import com.goatfarm.domain.BreedingRules;
import com.goatfarm.domain.Gender;
import com.goatfarm.entity.Goat;
import com.goatfarm.model.InbreedingCheckResponse;
import com.goatfarm.pedigree.InbreedingEvaluator;
import com.goatfarm.pedigree.PedigreeService;
import com.goatfarm.pedigree.PedigreeSnapshot;
import com.goatfarm.pedigree.PedigreeTreeBuilder;
import com.goatfarm.support.GoatLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/** "Can these two goats be bred?" including both family trees and, if not, a list of suitable alternatives. */
@Service
@Transactional(readOnly = true)
public class InbreedingService {

    private static final Logger log = LoggerFactory.getLogger(InbreedingService.class);

    private final GoatLookup goatLookup;
    private final PedigreeService pedigreeService;
    private final InbreedingEvaluator evaluator;

    public InbreedingService(GoatLookup goatLookup, PedigreeService pedigreeService, InbreedingEvaluator evaluator) {
        this.goatLookup = goatLookup;
        this.pedigreeService = pedigreeService;
        this.evaluator = evaluator;
    }

    public InbreedingCheckResponse checkInbreeding(String goatTagNumber, String breederTagNumber, Long farmId) {
        Goat goat = goatLookup.require(goatTagNumber, farmId, "Goat not found: " + goatTagNumber);
        Goat breeder = goatLookup.require(breederTagNumber, farmId, "Goat not found: " + breederTagNumber);

        // One query for the whole farm; every pedigree question below is answered in memory.
        PedigreeSnapshot pedigree = pedigreeService.snapshot(farmId);
        InbreedingCheckResponse verdict = evaluator.evaluate(goat, breeder, pedigree);

        int depth = BreedingRules.INBREEDING_CHECK_DEPTH;
        List<String> safeBreeders = verdict.isInbreeding() ? findSafeBreeders(goat, pedigree) : List.of();
        log.info("Inbreeding check: goatTagNumber={}, breederTagNumber={}, farmId={}, isInbreeding={}, safeBreeders={}",
                goatTagNumber, breederTagNumber, farmId, verdict.isInbreeding(), safeBreeders.size());
        return verdict.withPedigree(
                PedigreeTreeBuilder.build(goatTagNumber, depth, pedigree),
                PedigreeTreeBuilder.build(breederTagNumber, depth, pedigree),
                safeBreeders);
    }

    /** Males of the farm (other than the goat itself) that are not too closely related to it. */
    private List<String> findSafeBreeders(Goat goat, PedigreeSnapshot pedigree) {
        return pedigree.goats().stream()
                .filter(candidate -> Gender.MALE.matches(candidate.getGender()))
                .filter(candidate -> !Objects.equals(candidate.getTagNumber(), goat.getTagNumber()))
                .filter(candidate -> !evaluator.evaluate(goat, candidate, pedigree).isInbreeding())
                .map(Goat::getTagNumber)
                .toList();
    }
}
