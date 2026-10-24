package com.goatfarm.pedigree;

import com.goatfarm.entity.Goat;
import com.goatfarm.model.GoatTreeNode;
import org.junit.jupiter.api.Test;

import static com.goatfarm.pedigree.PedigreeFixtures.goat;
import static com.goatfarm.pedigree.PedigreeFixtures.snapshot;
import static org.assertj.core.api.Assertions.assertThat;

class PedigreeTreeBuilderTest {

    @Test
    void buildsParentsUpToDepth() {
        Goat gf = goat("GF", null, null, "MALE");
        Goat f = goat("F", "GF", null, "MALE");
        Goat kid = goat("KID", "F", null, "FEMALE");
        PedigreeSnapshot pedigree = snapshot(gf, f, kid);

        GoatTreeNode tree = PedigreeTreeBuilder.build("KID", 5, pedigree);

        assertThat(tree.getTagNumber()).isEqualTo("KID");
        assertThat(tree.getFather().getTagNumber()).isEqualTo("F");
        assertThat(tree.getFather().getFather().getTagNumber()).isEqualTo("GF");
        assertThat(tree.getMother()).isNull();
        assertThat(PedigreeTreeBuilder.build("KID", 2, pedigree).getFather().getFather()).isNull();
    }

    @Test
    void blankTagOrNonPositiveDepthGivesNull() {
        assertThat(PedigreeTreeBuilder.build("  ", 3, PedigreeSnapshot.empty())).isNull();
        assertThat(PedigreeTreeBuilder.build(null, 3, PedigreeSnapshot.empty())).isNull();
        assertThat(PedigreeTreeBuilder.build("A", 0, PedigreeSnapshot.empty())).isNull();
    }

    @Test
    void unregisteredParentBecomesAMarkedNode() {
        Goat kid = goat("KID", "GHOST", null, "FEMALE");

        GoatTreeNode father = PedigreeTreeBuilder.build("KID", 3, snapshot(kid)).getFather();

        assertThat(father.getTagNumber()).isEqualTo("GHOST");
        assertThat(father.getMissingReason()).isEqualTo("Parent record not registered in database");
    }

    @Test
    void sharedAncestorAppearsInBothBranches() {
        // Regression: a "visited" set shared by both branches used to drop the grandfather from the mother's side.
        Goat gf = goat("GF", null, null, "MALE");
        Goat f = goat("F", "GF", null, "MALE");
        Goat m = goat("M", "GF", null, "FEMALE");
        Goat kid = goat("KID", "F", "M", "FEMALE");

        GoatTreeNode tree = PedigreeTreeBuilder.build("KID", 5, snapshot(gf, f, m, kid));

        assertThat(tree.getFather().getFather().getTagNumber()).isEqualTo("GF");
        assertThat(tree.getMother().getFather().getTagNumber()).isEqualTo("GF");
        assertThat(tree.getMother().getFather().isCycleDetected()).isFalse();
    }

    @Test
    void realLoopIsFlaggedInsteadOfRecursingForever() {
        Goat a = goat("CA", "CB", null, "MALE");
        Goat b = goat("CB", "CA", null, "MALE");

        GoatTreeNode tree = PedigreeTreeBuilder.build("CA", 10, snapshot(a, b));

        GoatTreeNode loop = tree.getFather().getFather();
        assertThat(loop.isCycleDetected()).isTrue();
        assertThat(loop.getMissingReason()).isEqualTo("Pedigree loop/cycle detected");
    }
}
