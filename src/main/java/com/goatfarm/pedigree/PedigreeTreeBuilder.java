package com.goatfarm.pedigree;

import com.goatfarm.entity.Goat;
import com.goatfarm.model.GoatTreeNode;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Builds the family tree of a goat from a {@link PedigreeSnapshot}.
 *
 * <p>Uses "current path" cycle detection (backtracking), so an ancestor that legitimately appears in
 * both the father's and the mother's line is shown in both branches, while a true loop in the data is
 * reported once with {@code cycleDetected = true}.
 */
public final class PedigreeTreeBuilder {

    static final String CYCLE_REASON = "Pedigree loop/cycle detected";
    static final String MISSING_REASON = "Parent record not registered in database";

    private PedigreeTreeBuilder() {
    }

    /** @return the tree, or {@code null} when the tag is blank or {@code depth <= 0} */
    public static GoatTreeNode build(String tag, int depth, PedigreeSnapshot snapshot) {
        return build(tag, depth, new HashSet<>(), snapshot);
    }

    private static GoatTreeNode build(String tag, int depth, Set<String> currentPath, PedigreeSnapshot snapshot) {
        if (tag == null || tag.isBlank() || depth <= 0) {
            return null;
        }
        if (currentPath.contains(tag)) {
            GoatTreeNode cycleNode = new GoatTreeNode();
            cycleNode.setTagNumber(tag);
            cycleNode.setCycleDetected(true);
            cycleNode.setMissingReason(CYCLE_REASON);
            return cycleNode;
        }
        Optional<Goat> found = snapshot.find(tag);
        if (found.isEmpty()) {
            GoatTreeNode missingNode = new GoatTreeNode();
            missingNode.setTagNumber(tag);
            missingNode.setMissingReason(MISSING_REASON);
            return missingNode;
        }
        Goat goat = found.get();
        GoatTreeNode node = new GoatTreeNode();
        node.setTagNumber(goat.getTagNumber());
        node.setBreed(goat.getBreed());
        node.setGender(goat.getGender());
        node.setBirthDate(goat.getBirthDate());
        node.setWeight(goat.getWeight());
        node.setHeight(goat.getHeight());
        node.setCycleDetected(false);

        currentPath.add(tag);
        node.setFather(build(goat.getFatherTagNumber(), depth - 1, currentPath, snapshot));
        node.setMother(build(goat.getMotherTagNumber(), depth - 1, currentPath, snapshot));
        currentPath.remove(tag);
        return node;
    }
}
