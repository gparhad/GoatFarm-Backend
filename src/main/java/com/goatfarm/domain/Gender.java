package com.goatfarm.domain;

/** Gender values stored in the {@code gender} column (compared case-sensitively, as before). */
public enum Gender {
    MALE,
    FEMALE;

    public boolean matches(String value) {
        return name().equals(value);
    }
}
