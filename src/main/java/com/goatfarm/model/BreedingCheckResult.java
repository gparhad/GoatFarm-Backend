package com.goatfarm.model;

import java.util.Set;

public record BreedingCheckResult(boolean inbreeding, String message, Set<String> commonAncestors) {
}
