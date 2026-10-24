package com.goatfarm.support;

import java.util.function.Consumer;

public final class Values {

    private Values() {
    }

    /** Calls {@code setter} only when {@code value} is not null (partial-update helper). */
    public static <T> void ifNotNull(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
