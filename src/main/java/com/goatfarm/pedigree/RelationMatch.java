package com.goatfarm.pedigree;

import java.util.Set;

/** A kinship finding: the human readable message and the tags that make the two goats related. */
public record RelationMatch(String message, Set<String> sharedTags) {
}
