package com.goatfarm.pedigree;

import com.goatfarm.entity.Goat;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Immutable in-memory view of one farm's goats, indexed by tag number.
 *
 * <p>Pedigree questions (ancestors, parents, grandparents, trees) used to issue one database query
 * per ancestor. They are now answered from a single query's worth of data.
 */
public final class PedigreeSnapshot {

    private final Map<String, Goat> goatsByTag;

    private PedigreeSnapshot(Map<String, Goat> goatsByTag) {
        this.goatsByTag = goatsByTag;
    }

    public static PedigreeSnapshot of(Collection<Goat> goats) {
        Map<String, Goat> index = new LinkedHashMap<>();
        for (Goat goat : goats) {
            if (goat.getTagNumber() != null) {
                index.putIfAbsent(goat.getTagNumber(), goat);
            }
        }
        return new PedigreeSnapshot(index);
    }

    public static PedigreeSnapshot empty() {
        return new PedigreeSnapshot(Map.of());
    }

    public Optional<Goat> find(String tag) {
        return tag == null ? Optional.empty() : Optional.ofNullable(goatsByTag.get(tag));
    }

    /** All goats of the farm, in the order the repository returned them. */
    public Collection<Goat> goats() {
        return Collections.unmodifiableCollection(goatsByTag.values());
    }

    /** Parents, grandparents ... up to {@code depth} generations. Unregistered parents are still listed by tag. */
    public Set<String> ancestors(String tag, int depth) {
        Set<String> result = new HashSet<>();
        collectAncestors(tag, depth, result);
        return result;
    }

    public Set<String> parents(String tag) {
        return find(tag)
                .map(goat -> Stream.of(goat.getFatherTagNumber(), goat.getMotherTagNumber())
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()))
                .orElseGet(Set::of);
    }

    public Set<String> grandparents(String tag) {
        return parents(tag).stream()
                .flatMap(parent -> parents(parent).stream())
                .collect(Collectors.toSet());
    }

    private void collectAncestors(String tag, int depth, Set<String> accumulator) {
        if (tag == null || depth <= 0) {
            return;
        }
        Goat goat = goatsByTag.get(tag);
        if (goat == null) {
            return;
        }
        addParent(goat.getFatherTagNumber(), depth, accumulator);
        addParent(goat.getMotherTagNumber(), depth, accumulator);
    }

    private void addParent(String parentTag, int depth, Set<String> accumulator) {
        if (parentTag != null) {
            accumulator.add(parentTag);
            collectAncestors(parentTag, depth - 1, accumulator);
        }
    }
}
