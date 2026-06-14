package com.goatfarm.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class BreedingCheckResult {
    private boolean inbreeding;
    private String message;
    private Set<String> commonAncestors;
}
