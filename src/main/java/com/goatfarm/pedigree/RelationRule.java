package com.goatfarm.pedigree;

import com.goatfarm.entity.Goat;

import java.util.Optional;

/**
 * One way two goats can be too closely related. To teach the system a new kind of relation, add a rule
 * to {@link InbreedingEvaluator}; the evaluator itself does not change.
 */
@FunctionalInterface
public interface RelationRule {

    Optional<RelationMatch> detect(Goat goat, Goat breeder, PedigreeSnapshot pedigree);
}
