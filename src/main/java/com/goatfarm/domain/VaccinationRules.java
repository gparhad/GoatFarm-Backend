package com.goatfarm.domain;

/** Business constants for vaccination scheduling. */
public final class VaccinationRules {

    /** No two vaccines / dewormings may be given closer together than this. */
    public static final int MIN_DAYS_BETWEEN_DOSES = 21;

    /** Deworming is not allowed before the goat is this many months old. */
    public static final int DEWORMING_MIN_AGE_MONTHS = 3;

    /** Window used by {@code /schedule/tomorrow} (historically "now + 7 days", name kept for API compatibility). */
    public static final int DUE_SOON_OFFSET_DAYS = 7;

    /** Window used by {@code /upcoming}. */
    public static final int UPCOMING_WINDOW_DAYS = 7;

    private VaccinationRules() {
    }
}
