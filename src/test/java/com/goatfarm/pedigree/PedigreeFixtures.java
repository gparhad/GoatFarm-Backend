package com.goatfarm.pedigree;

import com.goatfarm.entity.Goat;

import java.util.Arrays;

/** Small helpers to build pedigrees in tests without a database. */
final class PedigreeFixtures {

    private PedigreeFixtures() {
    }

    static Goat goat(String tag, String father, String mother, String gender) {
        Goat goat = new Goat();
        goat.setTagNumber(tag);
        goat.setFatherTagNumber(father);
        goat.setMotherTagNumber(mother);
        goat.setGender(gender);
        return goat;
    }

    static PedigreeSnapshot snapshot(Goat... goats) {
        return PedigreeSnapshot.of(Arrays.asList(goats));
    }
}
