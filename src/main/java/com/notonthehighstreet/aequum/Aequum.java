package com.notonthehighstreet.aequum;

/*
 * #%L
 * Aequum Library
 * %%
 * Copyright (C) 2014 notonthehighstreet.com
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

/**
 * <p>
 *     A simple utility class to aid the creation of {@linkplain Object#equals(Object) equals}, {@linkplain Object#hashCode() hashCode} and
 *     {@linkplain Comparable#compareTo(Object) compareTo} methods. This utility class helps enforce the contract between the three methods such that
 *     if {@code a.equals(b)} then {@code a.hashCode() == b.hashCode() && a.compareTo(b) == 0} is always true. The classes created by this builder are thread safe.
 * </p>
 * <p>
 *     Example:
 * </p>
 * <pre>
 * public class Pojo implements Comparable&lt;Pojo&gt; {
 *
 *   private static final ComparableEqualsHashCode&lt;Pojo&gt; EQUALS_HASH_CODE = Aequum.builder(Pojo.class)
 *          .withComparableField(Pojo::getFieldOne)
 *          .withComparableField(Pojo::getFieldTwo)
 *          .build();
 *
 *   private final String fieldOne;
 *   private final String fieldTwo;
 *
 *   public Pojo(final String fieldOne, final String fieldTwo) {
 *     this.fieldOne = fieldOne;
 *     this.fieldTwo = fieldTwo;
 *   }
 *
 *   public String getFieldOne() {
 *     return fieldOne;
 *   }
 *
 *   public String getFieldTwo() {
 *     return fieldTwo;
 *   }
 *
 *   &#64;Override
 *   public boolean equals(final Object o) {
 *     return EQUALS_HASH_CODE.isEqual(this, o);
 *   }
 *
 *   &#64;Override
 *   public int hashCode() {
 *     return EQUALS_HASH_CODE.toHashCode(this);
 *   }
 *
 *   &#64;Override
 *   public int compareTo(final Pojo o) {
 *     return EQUALS_HASH_CODE.compare(this, o);
 *   }
 * }
 * </pre>
 * @see ComparableEqualsHashCode#compare(Object, Object)
 * @see EqualsHashCode#toHashCode(Object)
 * @see EqualsHashCode#isEqual(Object, Object)
 */
public class Aequum {

    /**
     * Builder for defining the contract for the {@linkplain Object#equals(Object) equals}, {@linkplain Object#hashCode() hashCode} and
     * optionally {@linkplain Comparable#compareTo(Object) compareTo} methods.
     * @param <T> Type of the class the methods will work on.
     */
    public static class Builder<T> {
        private final Class<T> expectedType;
        private final List<ComparableFieldValue<T>> fields;

        private Builder(final Class<T> expectedType) {
            this.expectedType = expectedType;
            this.fields = new ArrayList<>();
        }

        /**
         * Add an incomparable field for use in the {@linkplain Object#equals(Object) equals}, {@linkplain Object#hashCode() hashCode}, {@linkplain Object#toString()} toString}
         * methods. Note that {@linkplain Comparable#compareTo(Object) compareTo} will not be supported.
         * @param field Field or getter for a field.
         * @return A builder which will support {@linkplain Object#equals(Object) equals}, {@linkplain Object#hashCode() hashCode} and {@linkplain Object#toString()} toString}
         * but not support {@linkplain Comparable#compareTo(Object) compareTo}.
         */
        public IncomparableBuilder<T> withField(final SerializableFunction<T, Object> field) {
            return withField(field, field);
        }

        /**
         * Add an incomparable field for use in the {@linkplain Object#equals(Object) equals}, {@linkplain Object#hashCode() hashCode} and {@linkplain Object#toString()} toString}
         * methods. Note that {@linkplain Comparable#compareTo(Object) compareTo} will not be supported.
         * @param field Field or getter for a field.
         * @param toStringValue String representation of the field.
         * @return A builder which will support {@linkplain Object#equals(Object) equals}, {@linkplain Object#hashCode() hashCode} and {@linkplain Object#toString()} toString}
         * but not support {@linkplain Comparable#compareTo(Object) compareTo}.
         */
        public IncomparableBuilder<T> withField(final SerializableFunction<T, Object> field, final Function<T, ?> toStringValue) {
            return new IncomparableBuilder<>(expectedType, fields).withField(field, toStringValue);
        }

        /**
         * Add a comparable field for use in the {@linkplain Object#equals(Object) equals}, {@linkplain Object#hashCode() hashCode}, {@linkplain Object#toString()} toString} and
         * {@linkplain Comparable#compareTo(Object) compareTo} methods.
         * @param field Field or getter for a field.
         * @param <V> Type of the field which extends {@linkplain Comparable}
         * @return A builder which will support {@linkplain Object#equals(Object) equals}, {@linkplain Object#hashCode() hashCode}, {@linkplain Object#toString()} toString} and
         * {@linkplain Comparable#compareTo(Object) compareTo}.
         */
        public <V extends Comparable<V>> Builder<T> withComparableField(final SerializableFunction<T, V> field) {
            return withComparableField(field, Comparator.nullsFirst(Comparator.<V>naturalOrder()));
        }

        /**
         * Add a comparable field which is {@linkplain Optional} for use in the {@linkplain Object#equals(Object) equals}, {@linkplain Object#hashCode() hashCode},
         * {@linkplain Object#toString()} toString} and {@linkplain Comparable#compareTo(Object) compareTo} methods. This method assumes that the field will never be null.
         * @param field Field or getter for a field.
         * @param <V> Type of the field which extends {@linkplain Comparable}
         * @return A builder which will support {@linkplain Object#equals(Object) equals}, {@linkplain Object#hashCode() hashCode}, {@linkplain Object#toString()} toString} and
         * {@linkplain Comparable#compareTo(Object) compareTo}.
         */
        public <V extends Comparable<V>> Builder<T> withOptionalComparableField(final SerializableFunction<T, Optional<V>> field) {
            return withOptionalComparableField(field, Comparator.<V>naturalOrder());
        }

        /**
         * Add a incomparable field for use in the {@linkplain Object#equals(Object) equals}, {@linkplain Object#hashCode() hashCode}, {@linkplain Object#toString()} toString} and
         * {@linkplain Comparable#compareTo(Object) compareTo} methods but with a {@linkplain Comparator} to compare the field with.
         * @param field Field or getter for a field.
         * @param comparator {@linkplain Comparator} used to compare the field.
         * @param <V> Type of the field which extends {@linkplain Comparable}
         * @return A builder which will support {@linkplain Object#equals(Object) equals}, {@linkplain Object#hashCode() hashCode}, {@linkplain Object#toString()} toString} and
         * {@linkplain Comparable#compareTo(Object) compareTo}.
         */
        public <V> Builder<T> withComparableField(final SerializableFunction<T, V> field, final Comparator<V> comparator) {
            return withComparableField(field, comparator, field);
        }

        /**
         * Add a incomparable field for use in the {@linkplain Object#equals(Object) equals}, {@linkplain Object#hashCode() hashCode}, {@linkplain Object#toString()} toString} and
         * {@linkplain Comparable#compareTo(Object) compareTo} methods but with a {@linkplain Comparator} to compare the field with.
         * @param field Field or getter for a field.
         * @param comparator {@linkplain Comparator} used to compare the field.
         * @param toStringValue String representation of the field.
         * @param <V> Type of the field which extends {@linkplain Comparable}
         * @return A builder which will support {@linkplain Object#equals(Object) equals}, {@linkplain Object#hashCode() hashCode}, {@linkplain Object#toString()} toString} and
         * {@linkplain Comparable#compareTo(Object) compareTo}.
         */
        public <V> Builder<T> withComparableField(final SerializableFunction<T, V> field, final Comparator<V> comparator, final Function<T, ?> toStringValue) {
            fields.add(new ComparableFieldValue<>(true, field, comparator, toStringValue));
            return this;
        }

        /**
         * Add a incomparable field which is {@linkplain Optional} for use in the {@linkplain Object#equals(Object) equals}, {@linkplain Object#hashCode() hashCode},
         * {@linkplain Object#toString()} toString} and {@linkplain Comparable#compareTo(Object) compareTo} methods but with a {@linkplain Comparator} to compare the field with.
         * This method assumes that the field will never be null.
         * @param field Field or getter for a field.
         * @param comparator {@linkplain Comparator} used to compare the field.
         * @param <V> Type of the field which extends {@linkplain Comparable}
         * @return A builder which will support {@linkplain Object#equals(Object) equals}, {@linkplain Object#hashCode() hashCode}, {@linkplain Object#toString()} toString} and
         * {@linkplain Comparable#compareTo(Object) compareTo}.
         */
        public <V> Builder<T> withOptionalComparableField(final SerializableFunction<T, Optional<V>> field, final Comparator<V> comparator) {
            return withComparableField(field, new OptionalComparator<>(comparator));
        }

        /**
         * Add a field only for use in the {@linkplain Object#toString()} toString} method.
         * @param field Field or getter for a field.
         * @return A builder which will support {@linkplain Object#equals(Object) equals}, {@linkplain Object#hashCode() hashCode} &amp;
         * {@linkplain Object#toString()} toString} but not support {@linkplain Comparable#compareTo(Object) compareTo}.
         */
        public Builder<T> withToStringField(final SerializableFunction<T, ?> field) {
            return withToStringField(field, field);
        }

        /**
         * Add a field only for use in the {@linkplain Object#toString()} toString} method.
         * @param field Field or getter for a field. This will be used to retrieve the name of the field.
         * @param toStringValue String representation of the field.
         * @return A builder which will support {@linkplain Object#equals(Object) equals}, {@linkplain Object#hashCode() hashCode} &amp;
         * {@linkplain Object#toString()} toString} but not support {@linkplain Comparable#compareTo(Object) compareTo}.
         */
        public Builder<T> withToStringField(final SerializableFunction<T, ?> field, final Function<T, ?> toStringValue) {
            fields.add(new ComparableFieldValue<>(false, field, null, toStringValue));
            return this;
        }

        /**
         * Construct a {@linkplain ComparableEqualsHashCode} based on the details passed into the builder which can support the {@linkplain Object#equals(Object) equals},
         * {@linkplain Object#hashCode() hashCode}, {@linkplain Object#toString()} toString} and {@linkplain Comparable#compareTo(Object) compareTo} methods.
         * @return A newly constructed {@linkplain ComparableEqualsHashCode}.
         */
        public ComparableEqualsHashCode<T> build() {
            return new ComparableEqualsHashCode<>(fields, expectedType);
        }
    }

    /**
     * Builder for defining the contract of just the {@linkplain Object#equals(Object) equals} and {@linkplain Object#hashCode() hashCode} methods.
     * @param <T> Type of the class the method will work on.
     */
    public static class IncomparableBuilder<T> {
        private final Class<T> expectedType;
        private final List<FieldValue<T>> fields;

        private IncomparableBuilder(final Class<T> expectedType, final List<ComparableFieldValue<T>> fields) {
            this.expectedType = expectedType;
            this.fields = fields.stream().collect(toList());
        }

        /**
         * Add a field for use in the {@linkplain Object#equals(Object) equals}, {@linkplain Object#hashCode() hashCode} and
         * {@linkplain Object#toString()} toString} methods.
         * @param field Field or getter for a field.
         * @return A builder which will support {@linkplain Object#equals(Object) equals}, {@linkplain Object#hashCode() hashCode} &amp;
         * {@linkplain Object#toString()} toString} but not support {@linkplain Comparable#compareTo(Object) compareTo}.
         */
        public IncomparableBuilder<T> withField(final SerializableFunction<T, ?> field) {
            return withField(field, field);
        }

        /**
         * Add a field for use in the {@linkplain Object#equals(Object) equals}, {@linkplain Object#hashCode() hashCode} and
         * {@linkplain Object#toString()} toString} methods.
         * @param field Field or getter for a field.
         * @param toStringValue String representation of the field.
         * @return A builder which will support {@linkplain Object#equals(Object) equals}, {@linkplain Object#hashCode() hashCode} &amp;
         * {@linkplain Object#toString()} toString} but not support {@linkplain Comparable#compareTo(Object) compareTo}.
         */
        public IncomparableBuilder<T> withField(final SerializableFunction<T, ?> field, final Function<T, ?> toStringValue) {
            fields.add(new FieldValue<>(true, field, toStringValue));
            return this;
        }

        /**
         * Add a field only for use in the {@linkplain Object#toString()} toString} method.
         * @param field Field or getter for a field.
         * @return A builder which will support {@linkplain Object#equals(Object) equals}, {@linkplain Object#hashCode() hashCode} &amp;
         * {@linkplain Object#toString()} toString} but not support {@linkplain Comparable#compareTo(Object) compareTo}.
         */
        public IncomparableBuilder<T> withToStringField(final SerializableFunction<T, ?> field) {
            return withToStringField(field, field);
        }

        /**
         * Add a field only for use in the {@linkplain Object#toString()} toString} method.
         * @param field Field or getter for a field. This will be used to retrieve the name of the field.
         * @param toStringValue String representation of the field.
         * @return A builder which will support {@linkplain Object#equals(Object) equals}, {@linkplain Object#hashCode() hashCode} &amp;
         * {@linkplain Object#toString()} toString} but not support {@linkplain Comparable#compareTo(Object) compareTo}.
         */
        public IncomparableBuilder<T> withToStringField(final SerializableFunction<T, ?> field, final Function<T, ?> toStringValue) {
            fields.add(new FieldValue<>(false, field, toStringValue));
            return this;
        }

        /**
         * Construct an {@linkplain EqualsHashCode} based on the details passed into the builder which can support the {@linkplain Object#equals(Object) equals},
         * {@linkplain Object#hashCode() hashCode} and {@linkplain Object#toString()} toString} methods. Note that this will not support the
         * {@linkplain Comparable#compareTo(Object) compareTo} method.
         * @return A newly constructed {@linkplain EqualsHashCode}.
         */
        public EqualsHashCode<T> build() {
            return new EqualsHashCode<>(fields, expectedType);
        }
    }

    /**
     * Creates a new builder which is used to define the contract for the {@linkplain Object#equals(Object) equals}, {@linkplain Object#hashCode() hashCode} and
     * {@linkplain Comparable#compareTo(Object) compareTo} methods.
     * @param expectedType Class of the type that the methods will work on.
     * @param <T> Type of the clas that the methods will work on.
     * @return New builder to define the contract for the methods.
     */
    public static <T> Builder<T> builder(final Class<T> expectedType) {
        return new Builder<>(expectedType);
    }
}
