package com.goatfarm.pedigree;

import com.goatfarm.domain.BreedingRules;
import com.goatfarm.entity.Goat;
import com.goatfarm.model.InbreedingCheckResponse;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * Decides whether breeding {@code goat} with {@code breeder} would be inbreeding. Returns the bare verdict
 * ({@link InbreedingCheckResponse#verdict}); callers that need the family trees add them with
 * {@link InbreedingCheckResponse#withPedigree}.
 *
 * <p>Rules are evaluated in priority order; the first match provides the message. Every rule's shared tags
 * are merged into the reported common ancestors. Compared with the previous implementation:
 * parent/child is detected in both directions, and a grandparent/great-grandparent (any direct
 * ancestor within {@link BreedingRules#INBREEDING_CHECK_DEPTH} generations) is now detected too.
 */
@Component
public class InbreedingEvaluator {

    static final String PARENT_CHILD = "Parent-child relation detected!";
    static final String ANCESTOR_DESCENDANT = "Ancestor-descendant relation detected!";
    static final String SIBLING = "Sibling inbreeding detected!";
    static final String COUSIN = "A Cousin relation detected (avoid breeding)!";
    static final String SAFE = "Safe for breeding!";

    private final List<RelationRule> rules;

    public InbreedingEvaluator() {
        this(List.of(
                InbreedingEvaluator::parentChild,
                InbreedingEvaluator::ancestorDescendant,
                InbreedingEvaluator::siblings,
                InbreedingEvaluator::cousins));
    }

    InbreedingEvaluator(List<RelationRule> rules) {
        this.rules = rules;
    }

    public InbreedingCheckResponse evaluate(Goat goat, Goat breeder, PedigreeSnapshot pedigree) {
        int depth = BreedingRules.INBREEDING_CHECK_DEPTH;
        Set<String> common = new TreeSet<>(pedigree.ancestors(goat.getTagNumber(), depth));
        common.retainAll(pedigree.ancestors(breeder.getTagNumber(), depth));

        String message = null;
        boolean related = false;
        for (RelationRule rule : rules) {
            Optional<RelationMatch> match = rule.detect(goat, breeder, pedigree);
            if (match.isPresent()) {
                related = true;
                common.addAll(match.get().sharedTags());
                if (message == null) {
                    message = match.get().message();
                }
            }
        }
        if (message == null && !common.isEmpty()) {
            related = true;
            message = "Common ancestor found: " + common;
        }
        return InbreedingCheckResponse.verdict(related || !common.isEmpty(), message != null ? message : SAFE, common);
    }

    private static Optional<RelationMatch> parentChild(Goat goat, Goat breeder, PedigreeSnapshot pedigree) {
        Set<String> shared = new TreeSet<>();
        if (isParentOf(breeder.getTagNumber(), goat)) {
            shared.add(breeder.getTagNumber());
        }
        if (isParentOf(goat.getTagNumber(), breeder)) {
            shared.add(goat.getTagNumber());
        }
        return shared.isEmpty() ? Optional.empty() : Optional.of(new RelationMatch(PARENT_CHILD, shared));
    }

    private static boolean isParentOf(String candidateTag, Goat child) {
        return candidateTag != null
                && (candidateTag.equals(child.getFatherTagNumber()) || candidateTag.equals(child.getMotherTagNumber()));
    }

    private static Optional<RelationMatch> ancestorDescendant(Goat goat, Goat breeder, PedigreeSnapshot pedigree) {
        int depth = BreedingRules.INBREEDING_CHECK_DEPTH;
        Set<String> shared = new TreeSet<>();
        if (pedigree.ancestors(goat.getTagNumber(), depth).contains(breeder.getTagNumber())) {
            shared.add(breeder.getTagNumber());
        }
        if (pedigree.ancestors(breeder.getTagNumber(), depth).contains(goat.getTagNumber())) {
            shared.add(goat.getTagNumber());
        }
        return shared.isEmpty() ? Optional.empty() : Optional.of(new RelationMatch(ANCESTOR_DESCENDANT, shared));
    }

    private static Optional<RelationMatch> siblings(Goat goat, Goat breeder, PedigreeSnapshot pedigree) {
        return overlap(pedigree.parents(goat.getTagNumber()), pedigree.parents(breeder.getTagNumber()), SIBLING);
    }

    private static Optional<RelationMatch> cousins(Goat goat, Goat breeder, PedigreeSnapshot pedigree) {
        return overlap(pedigree.grandparents(goat.getTagNumber()), pedigree.grandparents(breeder.getTagNumber()), COUSIN);
    }

    private static Optional<RelationMatch> overlap(Set<String> first, Set<String> second, String message) {
        Set<String> shared = new HashSet<>(first);
        shared.retainAll(second);
        return shared.isEmpty() ? Optional.empty() : Optional.of(new RelationMatch(message, shared));
    }
}
