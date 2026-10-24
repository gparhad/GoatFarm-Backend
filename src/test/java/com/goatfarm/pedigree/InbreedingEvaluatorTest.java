package com.goatfarm.pedigree;

import com.goatfarm.entity.Goat;
import com.goatfarm.model.InbreedingCheckResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.goatfarm.pedigree.PedigreeFixtures.goat;
import static com.goatfarm.pedigree.PedigreeFixtures.snapshot;
import static org.assertj.core.api.Assertions.assertThat;

class InbreedingEvaluatorTest {

    // founders -> B1001 / M1001 -> G4001, G4002, B4003 (same pedigree shape as the seed data)
    private final Goat b001 = goat("B001", null, null, "MALE");
    private final Goat b002 = goat("B002", null, null, "MALE");
    private final Goat m001 = goat("M001", null, null, "FEMALE");
    private final Goat m002 = goat("M002", null, null, "FEMALE");
    private final Goat b1001 = goat("B1001", "B001", "M001", "MALE");
    private final Goat m1001 = goat("M1001", "B002", "M002", "FEMALE");
    private final Goat g4001 = goat("G4001", "B1001", "M1001", "FEMALE");
    private final Goat g4002 = goat("G4002", "B1001", "M1001", "FEMALE");
    private final Goat b4003 = goat("B4003", "B1001", "M1001", "MALE");
    private final Goat stranger = goat("S1", "X1", "X2", "MALE");

    private PedigreeSnapshot pedigree;
    private final InbreedingEvaluator evaluator = new InbreedingEvaluator();

    @BeforeEach
    void setUp() {
        pedigree = snapshot(b001, b002, m001, m002, b1001, m1001, g4001, g4002, b4003, stranger);
    }

    @Test
    void unrelatedGoatsAreSafe() {
        InbreedingCheckResponse result = evaluator.evaluate(g4001, stranger, pedigree);

        assertThat(result.isInbreeding()).isFalse();
        assertThat(result.message()).isEqualTo("Safe for breeding!");
    }

    @Test
    void fatherAndDaughterAreParentChild() {
        InbreedingCheckResponse result = evaluator.evaluate(g4001, b1001, pedigree);

        assertThat(result.isInbreeding()).isTrue();
        assertThat(result.message()).isEqualTo("Parent-child relation detected!");
        assertThat(result.commonAncestors()).contains("B1001");
    }

    @Test
    void damAndSonAreDetectedToo() {
        InbreedingCheckResponse result = evaluator.evaluate(m001, b1001, pedigree);

        assertThat(result.message()).isEqualTo("Parent-child relation detected!");
    }

    @Test
    void grandsireWithoutRecordedParentsIsDetected() {
        // Regression: this used to be reported as "Safe for breeding!" because the founder has no ancestors of his own.
        InbreedingCheckResponse result = evaluator.evaluate(g4001, b001, pedigree);

        assertThat(result.isInbreeding()).isTrue();
        assertThat(result.message()).isEqualTo("Ancestor-descendant relation detected!");
    }

    @Test
    void fullSiblingsAreDetected() {
        assertThat(evaluator.evaluate(g4001, b4003, pedigree).message()).isEqualTo("Sibling inbreeding detected!");
    }

    @Test
    void halfSiblingsAreReportedAsSiblings() {
        Goat first = goat("L1", "Z1", null, "FEMALE");
        Goat second = goat("L2", "Z9", "Z1", "MALE");

        assertThat(evaluator.evaluate(first, second, snapshot(first, second)).message())
                .isEqualTo("Sibling inbreeding detected!");
    }

    @Test
    void cousinsAreDetected() {
        Goat p1 = goat("P1", "B001", "M777", "MALE");
        Goat p2 = goat("P2", "B001", "M888", "FEMALE");
        Goat c1 = goat("C1", "P1", "M10", "MALE");
        Goat c2 = goat("C2", "B900", "P2", "FEMALE");

        InbreedingCheckResponse result = evaluator.evaluate(c1, c2, snapshot(b001, p1, p2, c1, c2));

        assertThat(result.message()).isEqualTo("A Cousin relation detected (avoid breeding)!");
        assertThat(result.commonAncestors()).contains("B001");
    }

    @Test
    void sharedGreatGrandparentOnlyGivesGenericMessage() {
        Goat z = goat("Z", null, null, "MALE");
        Goat y1 = goat("Y1", "Z", null, "MALE");
        Goat y2 = goat("Y2", "Y1", null, "MALE");
        Goat l3 = goat("L3", "Y2", null, "FEMALE");
        Goat w1 = goat("W1", "Z", null, "MALE");
        Goat w2 = goat("W2", "W1", null, "MALE");
        Goat l4 = goat("L4", "W2", null, "MALE");

        InbreedingCheckResponse result = evaluator.evaluate(l3, l4, snapshot(z, y1, y2, l3, w1, w2, l4));

        assertThat(result.isInbreeding()).isTrue();
        assertThat(result.message()).isEqualTo("Common ancestor found: [Z]");
    }

    @Test
    void commonAncestorsAreReportedInStableSortedOrder() {
        InbreedingCheckResponse result = evaluator.evaluate(g4001, b4003, pedigree);

        assertThat(List.copyOf(result.commonAncestors())).isSorted();
    }

    @Test
    void newRelationRulesPlugInWithoutChangingTheEvaluator() {
        InbreedingEvaluator custom = new InbreedingEvaluator(
                List.of((goat, breeder, snapshot) -> Optional.of(new RelationMatch("custom rule", Set.of("T")))));

        assertThat(custom.evaluate(g4001, stranger, pedigree).message()).isEqualTo("custom rule");
    }
}
