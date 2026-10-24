package com.goatfarm.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Set;

/**
 * Result of an inbreeding check. One class serves two purposes: the evaluator returns the bare
 * {@link #verdict verdict} (no trees), and the service enriches it with {@link #withPedigree the pedigree}
 * for {@code POST /api/breeding/check-inbreeding}.
 */
public record InbreedingCheckResponse(@JsonProperty("isInbreeding") boolean isInbreeding,
                                      String message,
                                      Set<String> commonAncestors,
                                      GoatTreeNode goatTree,
                                      GoatTreeNode breederTree,
                                      List<String> safeBreeders) {

    public static InbreedingCheckResponse verdict(boolean isInbreeding, String message, Set<String> commonAncestors) {
        return new InbreedingCheckResponse(isInbreeding, message, commonAncestors, null, null, List.of());
    }

    public InbreedingCheckResponse withPedigree(GoatTreeNode goatTree, GoatTreeNode breederTree, List<String> safeBreeders) {
        return new InbreedingCheckResponse(isInbreeding, message, commonAncestors, goatTree, breederTree, safeBreeders);
    }
}
