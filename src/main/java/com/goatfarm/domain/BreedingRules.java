package com.goatfarm.domain;

/** Business constants for breeding, kept in one place instead of magic numbers. */
public final class BreedingRules {

    /** Days between breeding and expected kidding. */
    public static final int GESTATION_DAYS = 150;

    /** How many generations are inspected when checking two goats for inbreeding. */
    public static final int INBREEDING_CHECK_DEPTH = 3;

    /** Default pedigree depth returned by the goat details endpoint. */
    public static final int DEFAULT_PEDIGREE_DEPTH = 5;

    private BreedingRules() {
    }
}
