package com.notonthehighstreet.aequum;

import java.util.Comparator;
import java.util.function.Function;

class ComparableFieldValue<T> extends FieldValue<T> {
    private final Comparator comparator;

    ComparableFieldValue(final boolean includedInEquality, final SerializableFunction<T, ?> field, final Comparator comparator,
                         final Function<T, ?> toStringValue) {
        super(includedInEquality, field, toStringValue);
        this.comparator = comparator;
    }

    public Comparator getComparator() {
        return comparator;
    }
}
