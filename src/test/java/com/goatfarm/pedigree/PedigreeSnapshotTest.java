package com.goatfarm.pedigree;

import com.goatfarm.entity.Goat;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.goatfarm.pedigree.PedigreeFixtures.goat;
import static com.goatfarm.pedigree.PedigreeFixtures.snapshot;
import static org.assertj.core.api.Assertions.assertThat;

class PedigreeSnapshotTest {

    private final Goat b001 = goat("B001", null, null, "MALE");
    private final Goat m001 = goat("M001", null, null, "FEMALE");
    private final Goat b1001 = goat("B1001", "B001", "M001", "MALE");
    private final Goat m1001 = goat("M1001", "B002", "M002", "FEMALE");
    private final Goat g4001 = goat("G4001", "B1001", "M1001", "FEMALE");
    private final PedigreeSnapshot pedigree = snapshot(b001, m001, b1001, m1001, g4001);

    @Test
    void ancestorsStopAtTheRequestedDepth() {
        assertThat(pedigree.ancestors("G4001", 1)).containsExactlyInAnyOrder("B1001", "M1001");
        assertThat(pedigree.ancestors("G4001", 2)).containsExactlyInAnyOrder("B1001", "M1001", "B001", "M001", "B002", "M002");
        assertThat(pedigree.ancestors("G4001", 0)).isEmpty();
    }

    @Test
    void unregisteredParentsAreStillListedByTag() {
        assertThat(pedigree.ancestors("M1001", 1)).containsExactlyInAnyOrder("B002", "M002");
    }

    @Test
    void unknownOrNullTagsGiveEmptyResults() {
        assertThat(pedigree.ancestors("NOPE", 3)).isEmpty();
        assertThat(pedigree.ancestors(null, 3)).isEmpty();
        assertThat(pedigree.parents("NOPE")).isEmpty();
        assertThat(pedigree.find(null)).isEmpty();
    }

    @Test
    void parentsAndGrandparents() {
        assertThat(pedigree.parents("G4001")).isEqualTo(Set.of("B1001", "M1001"));
        assertThat(pedigree.grandparents("G4001")).containsExactlyInAnyOrder("B001", "M001", "B002", "M002");
    }

    @Test
    void emptySnapshotAnswersEverythingWithNothing() {
        assertThat(PedigreeSnapshot.empty().goats()).isEmpty();
        assertThat(PedigreeSnapshot.empty().ancestors("G4001", 3)).isEmpty();
    }
}
