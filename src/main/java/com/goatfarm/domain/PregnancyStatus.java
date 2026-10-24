package com.goatfarm.domain;

/**
 * Known pregnancy states. The database column and the JSON API keep using the plain strings,
 * so this enum is only used internally to avoid scattering string literals.
 */
public enum PregnancyStatus {
    PREGNANT,
    DELIVERED,
    ABORTED;

    public boolean matches(String value) {
        return name().equals(value);
    }
}
