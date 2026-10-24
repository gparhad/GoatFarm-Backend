package com.goatfarm.vaccination;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Data-driven vaccination schedule: for each vaccine, which dose is due next and after how long.
 *
 * <p>Adding a vaccine is one line in {@link #buildRules()}; no control flow changes. Vaccine names are
 * matched exactly (case-sensitive), as before; anything unknown falls back to "Not Defined" in 3 months.
 */
@Component
public class VaccineScheduleCatalog {

    public static final String NOT_DEFINED = "Not Defined";

    /** What is due next and when. */
    public record NextDose(String vaccineName, LocalDate dueDate) {
    }

    private record Rule(String nextVaccineName, Period interval) {
    }

    private static final Rule DEFAULT_RULE = new Rule(NOT_DEFINED, Period.ofMonths(3));
    private static final Map<String, Rule> RULES = buildRules();

    public NextDose nextDose(String vaccineName, LocalDate givenOn) {
        Rule rule = vaccineName == null ? DEFAULT_RULE : RULES.getOrDefault(vaccineName, DEFAULT_RULE);
        return new NextDose(rule.nextVaccineName(), givenOn.plus(rule.interval()));
    }

    private static Map<String, Rule> buildRules() {
        Map<String, Rule> rules = new HashMap<>();
        rules.put("PPR", new Rule(NOT_DEFINED, Period.ofYears(3)));
        withBooster(rules, "FMD", Period.ofMonths(6));
        withBooster(rules, "Goat Pox", Period.ofYears(1));
        withBooster(rules, "Enterotoxemia", Period.ofYears(1));
        withBooster(rules, "Hemorrhagic Septicemia", Period.ofYears(1));
        rules.put(VaccinationPolicy.DEWORMING, new Rule(NOT_DEFINED, Period.ofMonths(3)));
        return Collections.unmodifiableMap(rules);
    }

    /** Base dose is followed by "<name> Booster" after 3 weeks; the booster is followed by "Not Defined" after {@code afterBooster}. */
    private static void withBooster(Map<String, Rule> rules, String baseName, Period afterBooster) {
        String boosterName = baseName + " Booster";
        rules.put(baseName, new Rule(boosterName, Period.ofWeeks(3)));
        rules.put(boosterName, new Rule(NOT_DEFINED, afterBooster));
    }
}
