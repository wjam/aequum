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

import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

/**
 * <p>
 *     Utility class that allows {@linkplain Object#equals(Object) equals} and {@linkplain Object#hashCode() hashCode} methods to be written simply and quickly.
 * </p>
 * <p>
 *     Example
 * </p>
 * <pre>
 *  private static final EqualsHashCode&lt;DeliveryGroup&gt; EQUALS_HASH_CODE = Aequum.builder(Pojo.class)
 *      .withField(o -&gt; o.fieldOne)
 *      .withField(Pojo::getFieldTwo)
 *      .build();
 * </pre>
 * <pre>
 *
 * &#64;Override
 * public boolean equals(Object o) {
 *   return EQUALS_HASH_CODE.isEqual(this, o);
 * }
 *
 * &#64;Override
 * public int hashCode() {
 *    return EQUALS_HASH_CODE.toHashCode(this);
 * }
 * </pre>
 * @param <T> Type that the equality and hash codes should be calculated on.
 */
public class EqualsHashCode<T> {

    private final SortedMap<String, Function<T, ?>> fieldNames;
    private final SerializableFunction<T, ?>[] fields;
    private final Class<T> expectedType;

    @SuppressWarnings("unchecked")
    EqualsHashCode(final Collection<? extends FieldValue<T>> fields, final Class<T> expectedType) {
        this.fields = fields.stream().filter(FieldValue::isIncludedInEquality).map(FieldValue::getField).toArray(SerializableFunction[]::new);
        this.expectedType = expectedType;

        fieldNames = fields.stream().collect(collectingAndThen(toMap(this::getAppropriateFieldName, FieldValue::getToStringValue), TreeMap::new));
    }

    private String getAppropriateFieldName(final FieldValue<T> f) {
        final MethodDetails method = f.getField().method();

        // Try the standard getter naming
        if (method.getMethodName().startsWith("get")) {
            return method.getMethodName().substring(3, 4).toLowerCase() + method.getMethodName().substring(4);
        }

        if (method.getMethodName().startsWith("is")) {
            return method.getMethodName().substring(2, 3).toLowerCase() + method.getMethodName().substring(3);
        }

        // Was it a lambda?
        if (method.getMethodName().contains("$")) {
            return getFieldNameFromLambdaMethod(method);
        }

        // Err...
        return method.getMethodName();
    }

    private String getFieldNameFromLambdaMethod(final MethodDetails method) {
        final FieldIdentifyingClassVisitor visitor = new FieldIdentifyingClassVisitor(method.getMethodName(), method.getMethodSignature());
        try {
            new ClassReader(method.getContainingClass()).accept(visitor, 0);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return visitor.getFieldName().get();
    }

    Stream<SerializableFunction<T, ?>> fields() {
        return Arrays.stream(fields);
    }

    /**
     * Check whether the given objects are equal.
     * @param thisObject <code>this</code> object.
     * @param thatObject Object to compare it to.
     * @return True if they are equal, false otherwise.
     * @see Object#equals(Object)
     */
    public boolean isEqual(final T thisObject, final Object thatObject) {
        if (thisObject == thatObject) {
            return true;
        }
        if (!expectedType.isInstance(thatObject)) {
            return false;
        }

        final T that = expectedType.cast(thatObject);

        return fields().allMatch(f -> Objects.deepEquals(f.apply(thisObject), f.apply(that)));
    }

    /**
     * Calculate the hash code for the given object.
     * @param thisObject <code>this</code> object.
     * @return The hash code value.
     * @see Object#hashCode()
     */
    public int toHashCode(final T thisObject) {
        final Stream<Object> map = fields().map(f -> f.apply(thisObject));
        return Arrays.deepHashCode(map.toArray(Object[]::new));
    }

    /**
     * Produce a {@code toString} value for the given object.
     * @param thisObject {@code this} object.
     * @return The string representation.
     * @see Object#toString()
     */
    public String toString(final T thisObject) {
        return fieldNames.entrySet().stream()
                .map(e -> e.getKey() + "=" + arraySafeToString(e.getValue().apply(thisObject)))
                .collect(joining(", ", thisObject.getClass().getSimpleName() + "{", "}"));
    }

    private String arraySafeToString(final Object o) {
        return o instanceof Object[] ? Arrays.toString((Object[]) o) : Objects.toString(o);
    }
}
