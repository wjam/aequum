package com.notonthehighstreet.aequum;

import java.util.function.Function;

class FieldValue<T> {

    private final boolean includedInEquality;
    private final SerializableFunction<T, ?> field;
    private final Function<T, ?> toStringValue;

    FieldValue(final boolean includedInEquality, final SerializableFunction<T, ?> field, final Function<T, ?> toStringValue) {
        this.includedInEquality = includedInEquality;
        this.field = field;
        this.toStringValue = toStringValue;
    }

    public SerializableFunction<T, ?> getField() {
        return field;
    }

    public Function<T, ?> getToStringValue() {
        return toStringValue;
    }

    public boolean isIncludedInEquality() {
        return includedInEquality;
    }
}
