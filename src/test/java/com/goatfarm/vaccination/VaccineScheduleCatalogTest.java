package com.goatfarm.vaccination;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class VaccineScheduleCatalogTest {

    private final VaccineScheduleCatalog catalog = new VaccineScheduleCatalog();
    private final LocalDate given = LocalDate.of(2026, 1, 31);

    @ParameterizedTest
    @CsvSource({
            "PPR,                              Not Defined,                       2029-01-31",
            "FMD,                              FMD Booster,                       2026-02-21",
            "FMD Booster,                      Not Defined,                       2026-07-31",
            "Goat Pox,                         Goat Pox Booster,                  2026-02-21",
            "Goat Pox Booster,                 Not Defined,                       2027-01-31",
            "Enterotoxemia,                    Enterotoxemia Booster,             2026-02-21",
            "Enterotoxemia Booster,            Not Defined,                       2027-01-31",
            "Hemorrhagic Septicemia,           Hemorrhagic Septicemia Booster,    2026-02-21",
            "Hemorrhagic Septicemia Booster,   Not Defined,                       2027-01-31",
            "Deworming,                        Not Defined,                       2026-04-30",
            "Something Else,                   Not Defined,                       2026-04-30"
    })
    void followsTheScheduleTable(String vaccine, String expectedNext, LocalDate expectedDate) {
        VaccineScheduleCatalog.NextDose next = catalog.nextDose(vaccine, given);

        assertThat(next.vaccineName()).isEqualTo(expectedNext);
        assertThat(next.dueDate()).isEqualTo(expectedDate);
    }

    @Test
    void matchingIsCaseSensitiveAndNullSafe() {
        assertThat(catalog.nextDose("fmd", given).vaccineName()).isEqualTo("Not Defined");
        assertThat(catalog.nextDose(null, given).dueDate()).isEqualTo(given.plusMonths(3));
    }
}
